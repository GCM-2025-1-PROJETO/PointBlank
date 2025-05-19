package br.imd.gcm.PointBlank.services

import br.imd.gcm.PointBlank.exception.AccountNotFoundException
import br.imd.gcm.PointBlank.model.Account
import br.imd.gcm.PointBlank.repositories.AccountRepository
import org.springframework.stereotype.Service
import br.imd.gcm.PointBlank.exception.DuplicateAccountException
import br.imd.gcm.PointBlank.exception.InsufficientBalanceException
import br.imd.gcm.PointBlank.exception.InvalidTransferAmountException
import br.imd.gcm.PointBlank.model.dto.AmountTransferDTO
import br.imd.gcm.PointBlank.model.dto.AmountTransferResponse
import java.math.BigDecimal

@Service
class AccountService(
    private val accountRepository: AccountRepository
) : BaseService<Account>(accountRepository) {

    fun requestAccount(): Account {
        val newAccountNumber = accountRepository.getLastID() + 1
        Account(
            number = newAccountNumber,
            balance = 0.0
        ).let { entity ->
            accountRepository.save(entity)
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

        accountRepository.save(originAccount)
        accountRepository.save(targetAccount)

        return AmountTransferResponse(
            originAccountNumber = originAccount.number,
            originAccountBalance = originAccount.balance,
            targetAccountNumber = targetAccount.number,
            targetAccountBalance = targetAccount.balance
        )
    }

}
