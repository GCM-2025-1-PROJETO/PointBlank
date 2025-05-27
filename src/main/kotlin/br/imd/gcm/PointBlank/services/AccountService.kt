package br.imd.gcm.PointBlank.services

import br.imd.gcm.PointBlank.exception.AccountNotFoundException
import br.imd.gcm.PointBlank.model.Account
import br.imd.gcm.PointBlank.repositories.AccountRepository
import org.springframework.stereotype.Service
import br.imd.gcm.PointBlank.exception.DuplicateAccountException
import br.imd.gcm.PointBlank.exception.InsufficientBalanceException
import br.imd.gcm.PointBlank.exception.InvalidTransferAmountException
import br.imd.gcm.PointBlank.model.BonusAccount
import br.imd.gcm.PointBlank.model.SavingsAccount
import br.imd.gcm.PointBlank.model.dto.AmountTransferDTO
import br.imd.gcm.PointBlank.model.dto.AmountTransferResponse
import br.imd.gcm.PointBlank.repositories.BonusAccountRepository
import br.imd.gcm.PointBlank.repositories.SavingsAccountRepository
import java.math.BigDecimal

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val bonusAccountRepository: BonusAccountRepository,
    private val savingsAccountRepository: SavingsAccountRepository
) : BaseService<Account>(accountRepository) {

    fun update(id: Long, updated: Account): Account {
        val existing = findByIdOrThrow(id)
        existing.number = updated.number
        existing.balance = updated.balance
        return save(existing)
    }

    fun requestAccount(): Account {
        val newAccountNumber = accountRepository.getLastID() + 1
        Account(
            number = newAccountNumber,
            balance = (request.balance ?: 0.0)
        ).let { entity ->
            accountRepository.save(entity)
            return entity
        }
    }

    fun requestBonusAccount(): BonusAccount {
        val newAccountNumber = bonusAccountRepository.getLastID() + 1
        BonusAccount(
            number = newAccountNumber,
            balance = 0.0
        ).let { entity ->
            bonusAccountRepository.save(entity)
            return entity
        }
    }

    fun requestSavingsAccount(): SavingsAccount {
        val newNumber = accountRepository.getLastID() + 1
        val acc = SavingsAccount(number = newNumber, balance = 0.0)
        return savingsAccountRepository.save(acc)
    }

    fun renderInterest(taxaPercentual: Double): List<SavingsAccount> {
        if(taxaPercentual < 0) {
            throw IllegalArgumentException("Taxa de juros deve ser >= 0")
        }
        val all = savingsAccountRepository.findAll()
        val fator = 1 + taxaPercentual / 100.0
        val updated = all.map { acct ->
            acct.balance = acct.balance * fator
            acct
        }
        return savingsAccountRepository.saveAll(updated)
    }

    fun getBalance(id: Long): Double {
        val account = findByIdOrThrow(id)
        return account.balance
    }

    fun credit(id: Long, amount: Double): Account {
        if(amount < 0) {
            throw IllegalArgumentException("O valor de crédito deve ser positivo")
        }

        val account = findByIdOrThrow(id)
        account.balance += amount

        if (account is BonusAccount) {
            account.points += (amount / 150).toInt()
            return bonusAccountRepository.save(account)
        }

        return save(account)
    }


    fun debit(accountId: Long, amount: Double): Account {

        if(amount < 0) {
            throw IllegalArgumentException("O valor do débito deve ser positivo")
        }

        val account = accountRepository.findById(accountId)
            .orElseThrow { RuntimeException("Conta não encontrada") }

        if(account.balance < amount) {
            throw InsufficientBalanceException("Saldo insuficiente para realizar o débito.")
        }

        account.balance -= amount
        return accountRepository.save(account)
    }

    fun transfer(transferRequest: AmountTransferDTO): AmountTransferResponse {
        if (transferRequest.amount <= 0) {
            throw InvalidTransferAmountException("O valor da transferência deve ser maior que zero.")
        }

        val originAccount = accountRepository.findById(transferRequest.originAccountNumber)
            .orElseThrow { AccountNotFoundException("Conta de origem não encontrada") }
        val targetAccount = accountRepository.findById(transferRequest.targetAccountNumber)
            .orElseThrow { AccountNotFoundException("Conta de destino não encontrada") }

        if (originAccount.balance < transferRequest.amount) {
            throw InsufficientBalanceException("Saldo insuficiente para a transferência")
        }

        originAccount.balance -= transferRequest.amount
        targetAccount.balance += transferRequest.amount

        if (targetAccount is BonusAccount) {
            targetAccount.points += (transferRequest.amount / 200).toInt()
            bonusAccountRepository.save(targetAccount)
        } else {
            accountRepository.save(targetAccount)
        }

        accountRepository.save(originAccount)

        return AmountTransferResponse(
            originAccountNumber = originAccount.number,
            originAccountBalance = originAccount.balance,
            targetAccountNumber = targetAccount.number,
            targetAccountBalance = targetAccount.balance
        )
    }

}
