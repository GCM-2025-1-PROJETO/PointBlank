package br.imd.gcm.PointBlank.model.dto.requests

data class AccountCreationRequest(
    val type: String,
    val balance: Double
)
