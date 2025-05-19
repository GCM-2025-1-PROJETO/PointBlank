package br.imd.gcm.PointBlank.repositories

import br.imd.gcm.PointBlank.model.AbstractEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseRepository<T : AbstractEntity> : JpaRepository<T, Long>
