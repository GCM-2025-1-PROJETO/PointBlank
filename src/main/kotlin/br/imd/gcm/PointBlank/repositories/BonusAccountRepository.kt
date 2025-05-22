package br.imd.gcm.PointBlank.repositories

import br.imd.gcm.PointBlank.model.BonusAccount
import org.springframework.data.jpa.repository.JpaRepository

interface BonusAccountRepository : JpaRepository<BonusAccount, Long>