package br.imd.gcm.PointBlank.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "ACCOUNT")
class Account(
    @Column(name = "NUMBER", nullable = false, unique = true) var number: Long,
    @Column(name = "BALANCE", nullable = false) var balance: Double
) : AbstractEntity() {
    constructor() : this(0L, 0.0)
}
