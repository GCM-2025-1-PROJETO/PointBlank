package br.imd.gcm.PointBlank.services

import br.imd.gcm.PointBlank.model.Account
import br.imd.gcm.PointBlank.repositories.AccountRepository
import org.springframework.stereotype.Service
import br.imd.gcm.PointBlank.exception.DuplicateAccountException
import java.math.BigDecimal

@Service
class AccountService(
    private val accountRepository: AccountRepository
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

}
