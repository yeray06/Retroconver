package com.retroconver.data.repository

import com.retroconver.data.local.AlbumConPortada
import com.retroconver.data.local.AlbumEntity
import com.retroconver.data.local.FotoEntity
import com.retroconver.data.local.GaleriaDao
import com.retroconver.data.local.UsuarioDao
import com.retroconver.data.local.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/*
 * Sustitutos de Room para las pruebas de los repositorios, que se ejecutan sin Android.
 * Guardan los datos en mapas que avisan de los cambios, como los Flow de Room.
 * Las consultas de verdad se prueban en el emulador (androidTest/…/data/local).
 */

internal class UsuarioDaoEnMemoria : UsuarioDao {

    val perfiles = MutableStateFlow<Map<String, UsuarioEntity>>(emptyMap())

    override suspend fun guardar(usuario: UsuarioEntity) {
        perfiles.value = perfiles.value + (usuario.id to usuario)
    }

    override suspend fun guardarTodos(usuarios: List<UsuarioEntity>) = usuarios.forEach { guardar(it) }

    override fun observar(id: String): Flow<UsuarioEntity?> = perfiles.map { it[id] }

    override suspend fun obtener(id: String): UsuarioEntity? = perfiles.value[id]

    override suspend fun actualizarDatos(id: String, nombre: String, biografia: String, estado: String) {
        perfiles.value[id]?.let { guardar(it.copy(nombre = nombre, biografia = biografia, estado = estado)) }
    }

    override suspend fun actualizarAvatar(id: String, avatarRuta: String?, avatarUri: String?) {
        perfiles.value[id]?.let { guardar(it.copy(avatarRuta = avatarRuta, avatarUri = avatarUri)) }
    }
}

internal class GaleriaDaoEnMemoria : GaleriaDao() {

    val albumes = MutableStateFlow<Map<String, AlbumEntity>>(emptyMap())
    val fotos = MutableStateFlow<Map<String, FotoEntity>>(emptyMap())

    override fun observarAlbumesDe(propietarioId: String): Flow<List<AlbumConPortada>> =
        combine(albumes, fotos) { todosLosAlbumes, todasLasFotos ->
            todosLosAlbumes.values
                .filter { it.propietarioId == propietarioId }
                .sortedByDescending { it.fechaCreacion }
                .map { album ->
                    val suyas = todasLasFotos.values.filter { it.albumId == album.id }
                    AlbumConPortada(album, suyas.size, suyas.maxByOrNull { it.fechaAnadida }?.uri)
                }
        }

    override fun observarAlbum(id: String): Flow<AlbumEntity?> = albumes.map { it[id] }

    override fun observarFotos(albumId: String): Flow<List<FotoEntity>> =
        fotos.map { todas -> todas.values.filter { it.albumId == albumId }.sortedByDescending { it.fechaAnadida } }

    override fun observarFoto(id: String): Flow<FotoEntity?> = fotos.map { it[id] }

    override suspend fun guardarAlbum(album: AlbumEntity) {
        albumes.value = albumes.value + (album.id to album)
    }

    override suspend fun guardarAlbumes(albumes: List<AlbumEntity>) = albumes.forEach { guardarAlbum(it) }

    override suspend fun guardarFoto(foto: FotoEntity) {
        fotos.value = fotos.value + (foto.id to foto)
    }

    override suspend fun guardarFotos(fotos: List<FotoEntity>) = fotos.forEach { guardarFoto(it) }

    override suspend fun borrarFoto(id: String) {
        fotos.value = fotos.value - id
    }

    override suspend fun borrarAlbum(id: String) {
        albumes.value = albumes.value - id
        fotos.value = fotos.value.filterValues { it.albumId != id }
    }

    override suspend fun borrarAlbumesQueYaNoEstan(propietarioId: String, conservar: List<String>) {
        albumes.value.values
            .filter { it.propietarioId == propietarioId && it.id !in conservar }
            .forEach { borrarAlbum(it.id) }
    }

    override suspend fun borrarFotosQueYaNoEstan(albumId: String, conservar: List<String>) {
        fotos.value = fotos.value.filterValues { it.albumId != albumId || it.id in conservar }
    }
}
