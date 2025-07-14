package br.imd.gcm.PointBlank

import br.imd.gcm.PointBlank.exception.InsufficientBalanceException
import br.imd.gcm.PointBlank.exception.InvalidTransferAmountException
import br.imd.gcm.PointBlank.model.BonusAccount
import br.imd.gcm.PointBlank.model.dto.AmountTransferDTO
import br.imd.gcm.PointBlank.model.dto.requests.AccountCreationRequest
import br.imd.gcm.PointBlank.repositories.AccountRepository
import br.imd.gcm.PointBlank.services.AccountService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AccountServiceTests {

    @Autowired
    private lateinit var service: AccountService

    @Autowired
    private lateinit var accountRepo: AccountRepository

    @BeforeEach
    fun cleanDatabase() {
        accountRepo.deleteAll()
    }

    /* ------------------------------------------------------------------ */
    /* 1. Criação de contas                                               */
    /* ------------------------------------------------------------------ */

    @Test
    fun `deve criar conta normal`() {
        val req = AccountCreationRequest(type = "normal")
        val acc = service.requestNormalAccount(req)

        assertNotNull(acc.id)
        assertEquals(0.0, acc.balance)
        assertTrue(acc.id!! > 0)
        assertTrue(acc.number > 0)
    }

    @Test
    fun `deve criar conta bonus`() {
        val acc = service.requestBonusAccount()

        assertTrue(acc is BonusAccount)
        assertEquals(10, (acc as BonusAccount).points)
        assertEquals(0.0, acc.balance)
    }

    @Test
    fun `deve criar conta poupanca`() {
        val req = AccountCreationRequest(type = "savings", balance = 250.0)
        val acc = service.requestSavingsAccount(req)

        assertEquals(250.0, acc.balance)
    }

    /* ------------------------------------------------------------------ */
    /* 2. Consulta de conta/saldo                                         */
    /* ------------------------------------------------------------------ */

    @Test
    fun `deve consultar conta por id`() {
        val req = AccountCreationRequest(type = "normal")
        val created = service.requestNormalAccount(req)
        val loaded = service.findByIdOrThrow(created.id!!)

        assertEquals(created.number, loaded.number)
        assertEquals(created.balance, loaded.balance)
    }

    @Test
    fun `deve consultar saldo corretamente`() {
        val req = AccountCreationRequest(type = "normal")
        val created = service.requestNormalAccount(req)
        service.credit(created.id!!, 500.0)

        val balance = service.getBalance(created.id!!)
        assertEquals(500.0, balance)
    }

    /* ------------------------------------------------------------------ */
    /* 3. Crédito                                                         */
    /* ------------------------------------------------------------------ */

    @Test
    fun `credito normal deve atualizar saldo`() {
        val req = AccountCreationRequest(type = "normal")
        val acc = service.requestNormalAccount(req)
        val updated = service.credit(acc.id!!, 200.0)

        assertEquals(200.0, updated.balance)
    }

    @Test
    fun `credito deve rejeitar valor negativo`() {
        val req = AccountCreationRequest(type = "normal")
        val acc = service.requestNormalAccount(req)
        assertThrows<IllegalArgumentException> { service.credit(acc.id!!, -50.0) }
    }

    @Test
    fun `credito deve adicionar pontos em conta bonus`() {
        val bonus = service.requestBonusAccount()
        val updated = service.credit(bonus.id!!, 250.0) as BonusAccount

        assertEquals(11, updated.points)
        assertEquals(250.0, updated.balance)
    }

    /* ------------------------------------------------------------------ */
    /* 4. Débito                                                          */
    /* ------------------------------------------------------------------ */

    @Test
    fun `debito normal deve atualizar saldo`() {
        val req = AccountCreationRequest(type = "normal")
        val acc = service.requestNormalAccount(req)
        service.credit(acc.id!!, 300.0)
        val updated = service.debit(acc.id!!, 100.0)

        assertEquals(200.0, updated.balance)
    }

    @Test
    fun `debito deve rejeitar valor negativo`() {
        val req = AccountCreationRequest(type = "normal")
        val acc = service.requestNormalAccount(req)
        assertThrows<IllegalArgumentException> { service.debit(acc.id!!, -10.0) }
    }

    @Test
    fun `debito deve impedir saldo menor que -2000`() {
        val req = AccountCreationRequest(type = "normal")
        val acc = service.requestNormalAccount(req)
        assertThrows<InsufficientBalanceException> { service.debit(acc.id!!, 2001.0) }
    }

    /* ------------------------------------------------------------------ */
    /* 5. Transferência                                                   */
    /* ------------------------------------------------------------------ */

    @Test
    fun `transferencia deve rejeitar valor negativo`() {
        val reqA = AccountCreationRequest(type = "normal")
        val reqB = AccountCreationRequest(type = "normal")
        val a = service.requestNormalAccount(reqA)
        val b = service.requestNormalAccount(reqB)

        val dto = AmountTransferDTO(a.id!!, b.id!!, -1.0)
        assertThrows<InvalidTransferAmountException> { service.transfer(dto) }
    }

    @Test
    fun `transferencia deve impedir saldo menor que -2000 na origem`() {
        val reqOrigem = AccountCreationRequest(type = "normal")
        val reqDestino = AccountCreationRequest(type = "normal")
        val origem = service.requestNormalAccount(reqOrigem)
        val destino = service.requestNormalAccount(reqDestino)

        val dto = AmountTransferDTO(origem.id!!, destino.id!!, 2001.0)
        assertThrows<InsufficientBalanceException> { service.transfer(dto) }
    }

    @Test
    fun `transferencia para conta bonus deve adicionar pontos`() {
        val reqOrigem = AccountCreationRequest(type = "normal")
        val origem = service.requestNormalAccount(reqOrigem)
        service.credit(origem.id!!, 1000.0)

        val destino = service.requestBonusAccount()
        val dto = AmountTransferDTO(origem.id!!, destino.id!!, 400.0)
        val response = service.transfer(dto)

        val destinoAtualizado = accountRepo.findById(destino.id!!).get() as BonusAccount

        assertEquals(12, destinoAtualizado.points)
        assertEquals(600.0, response.originAccountBalance)
        assertEquals(400.0, response.targetAccountBalance)
    }

    /* ------------------------------------------------------------------ */
    /* 6. Renderização de Juros                                           */
    /* ------------------------------------------------------------------ */

    @Test
    fun `render juros deve atualizar todas as poupancas`() {
        val req1 = AccountCreationRequest(type = "savings", balance = 100.0)
        val req2 = AccountCreationRequest(type = "savings", balance = 200.0)
        val p1 = service.requestSavingsAccount(req1)
        val p2 = service.requestSavingsAccount(req2)

        val resultado = service.renderInterest(10.0) // 10%

        val atualizado1 = resultado.first { it.id == p1.id }
        val atualizado2 = resultado.first { it.id == p2.id }

        assertEquals(110.0, atualizado1.balance, 0.0001)
        assertEquals(220.0, atualizado2.balance, 0.0001)
    }
}
