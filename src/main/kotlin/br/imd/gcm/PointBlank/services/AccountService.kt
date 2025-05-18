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

    override fun save(entity: Account): Account {
        accountRepository.findByNumber(entity.number)?.let {
            throw DuplicateAccountException("Account number ${entity.number} already exists")
        }
        return accountRepository.save(entity)
    }
}
