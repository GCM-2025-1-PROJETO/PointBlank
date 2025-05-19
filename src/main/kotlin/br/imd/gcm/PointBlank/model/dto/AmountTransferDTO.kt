package br.imd.gcm.PointBlank.model.dto

class AmountTransferDTO(
    var originAccountNumber: Long,
    var targetAccountNumber: Long,
    var amount: Double
) {
    constructor() : this(0L, 0L, 0.0)
}
