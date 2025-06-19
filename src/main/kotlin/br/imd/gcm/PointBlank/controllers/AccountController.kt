package br.imd.gcm.PointBlank.controllers

import br.imd.gcm.PointBlank.exception.AccountNotFoundException
import br.imd.gcm.PointBlank.exception.DuplicateAccountException
import br.imd.gcm.PointBlank.exception.InsufficientBalanceException
import br.imd.gcm.PointBlank.exception.InvalidTransferAmountException
import br.imd.gcm.PointBlank.model.Account
import br.imd.gcm.PointBlank.model.dto.AmountTransferDTO
import br.imd.gcm.PointBlank.model.dto.requests.AccountCreationRequest
import br.imd.gcm.PointBlank.services.AccountService
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/accounts")
class AccountController(private val accountService: AccountService) {

    @GetMapping
    fun getAll(): ResponseEntity<List<Account>> =
        ResponseEntity.ok(accountService.findAll())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Account> =
        accountService.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/{id}/balance")
    fun getBalance(@PathVariable id: Long): ResponseEntity<Map<String, Double>> {
        return try {
            val balance = accountService.getBalance(id)
            ResponseEntity.ok(mapOf("balance" to balance))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/request")
    fun create(@RequestBody request: AccountCreationRequest): ResponseEntity<Any> {
        return try {
            val account: Account = when (request.type.lowercase()) {
                "normal"  -> accountService.requestNormalAccount(request)
                "bonus"   -> accountService.requestBonusAccount()
                "savings", "poupanca" ->
                    accountService.requestSavingsAccount(request)
                else -> return ResponseEntity
                    .badRequest()
                    .body(mapOf("error" to "Tipo de conta inválido: ${request.type}"))
            }
            ResponseEntity.status(HttpStatus.CREATED).body(account)
        } catch (e: DuplicateAccountException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}/credit")
    fun credit(
        @PathVariable id: Long,
        @RequestParam amount: Double
    ): ResponseEntity<Account> {
        return try {
            val updated = accountService.credit(id, amount)
            ResponseEntity.ok(updated)
        } catch (ex: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (ex: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(null)
        }
    }

    @PutMapping("/{id}/debit")
    fun debit(@PathVariable id: Long, @RequestParam amount: Double): ResponseEntity<Account> {
        val updatedAccount = accountService.debit(id, amount)
        return ResponseEntity.ok(updatedAccount)
    }

    @PutMapping("/transfer")
    fun transfer(
        @RequestBody transferRequest: AmountTransferDTO
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok().body(accountService.transfer(transferRequest))
        } catch (e: AccountNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: InsufficientBalanceException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        } catch (e: InvalidTransferAmountException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @PutMapping("/render-juros")
    fun renderJuros(@RequestParam taxa: Double): ResponseEntity<Any> {
        return try {
            val updatedList = accountService.renderInterest(taxa)
            ResponseEntity.ok(updatedList)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }
}
