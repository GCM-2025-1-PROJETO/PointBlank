package br.imd.gcm.PointBlank.model

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "BONUS_ACCOUNT")
@DiscriminatorValue("BONUS")
class BonusAccount(
    number: Long,
    balance: Double,
    @Column(name = "POINTS", nullable = false)
    var points: Int = 10
) : Account(number, balance) {
    constructor() : this(0L, 0.0, 10)
}