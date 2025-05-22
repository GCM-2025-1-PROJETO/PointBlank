package br.imd.gcm.PointBlank.repositories

import br.imd.gcm.PointBlank.model.Account
import br.imd.gcm.PointBlank.model.BonusAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BonusAccountRepository : BaseRepository<BonusAccount> {
    fun findByNumber(number: Long): Account?

    @Query(nativeQuery = true, value = "SELECT COALESCE(MAX(A.NUMBER), 0) FROM ACCOUNT A")
    fun getLastID(): Long
}
