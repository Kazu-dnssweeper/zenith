package com.iterio.app.data.mapper

/**
 * Entity と Domain Model の双方向変換を行うインターフェース
 *
 * @param Entity Room Entity 型
 * @param Domain Domain Model 型
 *
 * Usage:
 * ```
 * class TaskMapper : Mapper<TaskEntity, Task> {
 *     override fun toDomain(entity: TaskEntity): Task = ...
 *     override fun toEntity(domain: Task): TaskEntity = ...
 * }
 * ```
 */
interface Mapper<Entity, Domain> {
    /**
     * Entity から Domain Model に変換
     */
    fun toDomain(entity: Entity): Domain

    /**
     * Domain Model から Entity に変換
     */
    fun toEntity(domain: Domain): Entity

    /**
     * Entity リストから Domain Model リストに変換
     */
    fun toDomainList(entities: List<Entity>): List<Domain> = entities.map { toDomain(it) }

    /**
     * Domain Model リストから Entity リストに変換
     */
    fun toEntityList(domains: List<Domain>): List<Entity> = domains.map { toEntity(it) }
}
