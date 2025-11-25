package com.example.tiendastore.data

import android.content.Context
import android.net.Uri
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.tiendastore.model.Product
import com.example.tiendastore.model.User
import com.example.tiendastore.model.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

// Entities
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val stock: Int,
    val category: String,
    val description: String = "",
    val imagePath: String? = null
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val password: String,
    val isAdmin: Boolean = false,
    val name: String = "",
    val address: String = "",
    val city: String = ""
)

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 0,
    val email: String
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: Int,
    val name: String,
    val price: Double,
    val qty: Int,
    val imagePath: String? = null
)

// DAOs
@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllOnce(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Int): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getByIdOnce(id: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE CAST(id AS TEXT) LIKE '%' || :idQuery || '%' ORDER BY id ASC")
    fun searchByIdLike(idQuery: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProductEntity): Long

    @Update
    suspend fun update(entity: ProductEntity)

    @Delete
    suspend fun delete(entity: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: UserEntity)

    @Update
    suspend fun update(entity: UserEntity)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE id = 0")
    fun observe(): Flow<SessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(session: SessionEntity)

    @Query("DELETE FROM session")
    suspend fun clear()
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :id")
    suspend fun getByIdOnce(id: Int): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CartItemEntity)

    @Query("UPDATE cart_items SET qty = :qty WHERE productId = :id")
    suspend fun updateQty(id: Int, qty: Int)

    @Query("DELETE FROM cart_items WHERE productId = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}

// Database
@Database(
    entities = [ProductEntity::class, UserEntity::class, SessionEntity::class, CartItemEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            val inst = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tiendastore.db"
            ).fallbackToDestructiveMigration().build()
            INSTANCE = inst
            inst
        }
    }
}

// Image storage helper (internal app storage)
object ImageStorage {
    private fun dir(context: Context): File = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }

    suspend fun saveForProduct(context: Context, productId: Int, source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val ext = "jpg" // simple default; si quieres, detecta MIME
            val outFile = File(dir(context), "product_${productId}.${ext}")
            context.contentResolver.openInputStream(source).use { input ->
                FileOutputStream(outFile).use { output ->
                    if (input != null) input.copyTo(output)
                }
            }
            outFile.absolutePath
        }.getOrNull()
    }

    fun deletePath(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return runCatching { File(path).takeIf { it.exists() }?.delete() ?: false }.getOrDefault(false)
    }
}

// Mapping helpers
fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    price = price,
    stock = stock,
    category = category,
    description = description,
    imagePath = imagePath
)

fun Product.toEntity(existingImagePath: String? = null, newImagePath: String? = null): ProductEntity = ProductEntity(
    id = id,
    name = name,
    price = price,
    stock = stock,
    category = category,
    description = description,
    imagePath = newImagePath ?: imagePath ?: existingImagePath
)

fun UserEntity.toDomain(): User = User(
    username = email, // mantenemos compatibilidad username=email
    password = password,
    isAdmin = isAdmin,
    name = name,
    email = email,
    address = address,
    city = city
)

fun User.toEntity(): UserEntity = UserEntity(
    email = email.ifBlank { username },
    password = password,
    isAdmin = isAdmin,
    name = name,
    address = address,
    city = city
)

fun CartItemEntity.toDomain(): CartItem = CartItem(
    productId = productId,
    name = name,
    price = price,
    qty = qty,
    imagePath = imagePath
)

fun CartItem.toEntity(): CartItemEntity = CartItemEntity(
    productId = productId,
    name = name,
    price = price,
    qty = qty,
    imagePath = imagePath
)

// Facade helper para operaciones comunes ligadas a imágenes
object DataBaseHelper {
    fun db(context: Context) = AppDatabase.get(context)

    suspend fun upsertProductWithOptionalImage(
        context: Context,
        product: Product,
        newImageUri: Uri?
    ): Int {
        val dao = db(context).productDao()
        // Si el producto no existe aún, insertamos primero para obtener ID
        if (product.id == 0) {
            val tempId = dao.insert(product.toEntity())
            val newId = tempId.toInt()
            val savedPath = if (newImageUri != null) ImageStorage.saveForProduct(context, newId, newImageUri) else null
            dao.update(product.copy(id = newId).toEntity(newImagePath = savedPath))
            return newId
        } else {
            val existing = dao.getByIdOnce(product.id)
            val savedPath = if (newImageUri != null) ImageStorage.saveForProduct(context, product.id, newImageUri) else existing?.imagePath
            dao.update(product.toEntity(existingImagePath = existing?.imagePath, newImagePath = savedPath))
            return product.id
        }
    }

    suspend fun deleteProduct(context: Context, id: Int) {
        val dao = db(context).productDao()
        val existing = dao.getByIdOnce(id)
        if (existing != null) {
            dao.delete(existing)
            ImageStorage.deletePath(existing.imagePath)
        }
    }
}
