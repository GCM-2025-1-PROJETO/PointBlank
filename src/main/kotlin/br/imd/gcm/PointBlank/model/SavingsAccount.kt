package br.imd.gcm.PointBlank.model

import jakarta.persistence.*

@Entity
@Table(name = "SAVINGS_ACCOUNT")
@DiscriminatorValue("SAVINGS")
class SavingsAccount(
    number: Long,
    balance: Double
) : Account(number, balance) {
    constructor() : this(0L, 0.0)
}
