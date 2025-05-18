package br.imd.gcm.PointBlank.services

import br.imd.gcm.PointBlank.model.Account
import br.imd.gcm.PointBlank.repositories.AccountRepository
import org.springframework.stereotype.Service
import br.imd.gcm.PointBlank.exception.DuplicateAccountException

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
}
