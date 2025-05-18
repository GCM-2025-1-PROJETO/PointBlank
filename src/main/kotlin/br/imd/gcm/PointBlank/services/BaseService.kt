package br.imd.gcm.PointBlank.services

import br.imd.gcm.PointBlank.model.AbstractEntity
import br.imd.gcm.PointBlank.repositories.BaseRepository
import org.springframework.data.domain.Sort
import java.util.*

abstract class BaseService<T : AbstractEntity>(
    private val repository: BaseRepository<T>
) {

    fun findAll(): List<T> = repository.findAll()

    fun findAllSortedByCreatedAt(): List<T> =
        repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))

    fun findAllSortedByUpdatedAt(): List<T> =
        repository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))

    fun findById(id: Long): Optional<T> = repository.findById(id)

    fun findByIdOrThrow(id: Long): T =
        repository.findById(id).orElseThrow { NoSuchElementException("ID $id não encontrado") }

    fun save(entity: T): T = repository.save(entity)

    fun deleteById(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("ID $id não encontrado")
        }
        repository.deleteById(id)
    }
}