package br.imd.gcm.PointBlank.repositories

import br.imd.gcm.PointBlank.model.Account
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : BaseRepository<Account> {
    fun findByNumber(number: Long): Account?
}