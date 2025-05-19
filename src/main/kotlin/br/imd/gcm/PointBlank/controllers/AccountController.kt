package br.imd.gcm.PointBlank.controllers

import br.imd.gcm.PointBlank.exception.DuplicateAccountException
import br.imd.gcm.PointBlank.model.Account
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
    fun create(): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(accountService.requestAccount())
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

    @PostMapping("/{id}/debit")
    fun debit(@PathVariable accountId: Long, @RequestParam amount: Double): ResponseEntity<Account> {
        val updatedAccount = accountService.debit(accountId, amount)
        return ResponseEntity.ok(updatedAccount)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody account: Account): ResponseEntity<Account> =
        ResponseEntity.ok(accountService.update(id, account))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        accountService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
