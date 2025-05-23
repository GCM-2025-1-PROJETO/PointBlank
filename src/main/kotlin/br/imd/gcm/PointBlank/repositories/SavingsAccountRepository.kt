package br.imd.gcm.PointBlank.repositories

import br.imd.gcm.PointBlank.model.SavingsAccount
import org.springframework.data.jpa.repository.JpaRepository

interface SavingsAccountRepository : JpaRepository<SavingsAccount, Long>
