package br.imd.gcm.PointBlank.services

import br.imd.gcm.PointBlank.exception.AccountNotFoundException
import br.imd.gcm.PointBlank.model.Account
import br.imd.gcm.PointBlank.repositories.AccountRepository
import org.springframework.stereotype.Service
import br.imd.gcm.PointBlank.exception.DuplicateAccountException
import br.imd.gcm.PointBlank.exception.InsufficientBalanceException
import br.imd.gcm.PointBlank.exception.InvalidTransferAmountException
import br.imd.gcm.PointBlank.model.BonusAccount
import br.imd.gcm.PointBlank.model.dto.AmountTransferDTO
import br.imd.gcm.PointBlank.model.dto.AmountTransferResponse
import br.imd.gcm.PointBlank.repositories.BonusAccountRepository
import java.math.BigDecimal

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val bonusAccountRepository: BonusAccountRepository
) : BaseService<Account>(accountRepository) {

    fun requestNormalAccount(): Account {
        val newAccountNumber = accountRepository.getLastID() + 1
        Account(
            number = newAccountNumber,
            balance = 0.0
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

    fun getBalance(id: Long): Double {
        val account = findByIdOrThrow(id)
        return account.balance
    }

    fun credit(id: Long, amount: Double): Account {
        require(amount > 0) { "O valor de crédito deve ser positivo" }

        val account = findByIdOrThrow(id)
        account.balance += amount

        if (account is BonusAccount) {
            account.points += (amount / 100).toInt()
            return bonusAccountRepository.save(account)
        }

        return save(account)
    }


    fun debit(accountId: Long, amount: Double): Account {
        require(amount > 0) { "O valor do débito deve ser maior que zero." }

        val account = accountRepository.findById(accountId)
            .orElseThrow { RuntimeException("Conta não encontrada") }

        account.balance = account.balance - amount
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
