package br.imd.gcm.PointBlank.repositories

import br.imd.gcm.PointBlank.model.Account
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : BaseRepository<Account> {
    fun findByNumber(number: Long): Account?

    @Query(nativeQuery = true, value = "SELECT COALESCE(MAX(A.NUMBER), 0) FROM ACCOUNT A")
    fun getLastID(): Long
}