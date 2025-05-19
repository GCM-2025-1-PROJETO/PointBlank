package br.imd.gcm.PointBlank.model.dto

class AmountTransferResponse(
    var originAccountNumber: Long,
    var originAccountBalance: Double,
    var targetAccountNumber: Long,
    var targetAccountBalance: Double
) {
    constructor() : this(0L, 0.0, 0L, 0.0) {
    }
}
