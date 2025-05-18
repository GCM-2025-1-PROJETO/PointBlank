package br.imd.gcm.PointBlank.services

import br.imd.gcm.PointBlank.model.Account
import br.imd.gcm.PointBlank.repositories.AccountRepository
import org.springframework.stereotype.Service

@Service
class AccountService(
    accountRepository: AccountRepository
) : BaseService<Account>(accountRepository) {

    fun update(id: Long, updated: Account): Account {
        val existing = findByIdOrThrow(id)
        existing.number = updated.number
        existing.balance = updated.balance
        return save(existing)
    }
}