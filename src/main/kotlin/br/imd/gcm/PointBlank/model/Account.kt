package br.imd.gcm.PointBlank.model

import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Account(
    @Column(name = "NUMBER", nullable = false, unique = true) var number: Long,
    @Column(name = "BALANCE", nullable = false) var balance: Double
) : AbstractEntity() {
    constructor() : this(0L, 0.0)
}
