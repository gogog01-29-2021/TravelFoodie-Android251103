# TravelFoodie - Detailed Implementation Documentation

**TravelFoodie** is an Android travel planning application with AI-powered attraction and restaurant recommendations.

---

## 4. Restaurant Detail & Google Maps Navigation

### 📂 File Structure
```
feature/restaurant/src/main/java/com/travelfoodie/feature/restaurant/
├── RestaurantListFragment.kt  (UI Controller)
├── RestaurantAdapter.kt        (RecyclerView Adapter with Maps integration)
└── RestaurantViewModel.kt      (Business Logic)

core/data/src/main/java/com/travelfoodie/core/data/
├── local/entity/Entities.kt    (Line 95-121: RestaurantEntity)
└── repository/RestaurantRepository.kt
```

### ✅ **IMPLEMENTED**: Click Restaurant → Open in Google Maps

**File:** `feature/restaurant/src/main/java/com/travelfoodie/feature/restaurant/RestaurantAdapter.kt`

**Lines 58-76:** Google Maps intent logic
```kotlin
private fun openInGoogleMaps(restaurant: RestaurantEntity) {
    // Create a search query for Google Maps
    val searchQuery = Uri.encode(restaurant.name)
    val gmmIntentUri = Uri.parse("geo:0,0?q=$searchQuery")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")

    // Check if Google Maps is installed
    if (mapIntent.resolveActivity(binding.root.context.packageManager) != null) {
        binding.root.context.startActivity(mapIntent)
    } else {
        // Fallback to web browser if Google Maps is not installed
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$searchQuery")
        )
        binding.root.context.startActivity(browserIntent)
    }
}
```

**Lines 44-48:** Click handler that triggers Maps
```kotlin
// Click to open in Google Maps
root.setOnClickListener {
    openInGoogleMaps(restaurant)
    onRestaurantClick?.invoke(restaurant)
}
```

### ✅ **DATA STRUCTURE**: Restaurant Entity with Menu/Hours/Reservable

**File:** `core/data/src/main/java/com/travelfoodie/core/data/local/entity/Entities.kt`

**Lines 95-121:** RestaurantEntity definition
```kotlin
@Parcelize
@Entity(
    tableName = "restaurants",
    foreignKeys = [ForeignKey(
        entity = RegionEntity::class,
        parentColumns = ["regionId"],
        childColumns = ["regionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("regionId")]
)
data class RestaurantEntity(
    @PrimaryKey val restaurantId: String,
    val regionId: String,
    val name: String,
    val category: String,
    val rating: Float,
    val distance: Double?,
    val lat: Double,
    val lng: Double,
    val menu: String?,              // ✅ Menu stored here
    val hours: String?,             // ✅ Business hours stored here
    val reservable: Boolean = false, // ✅ Reservation availability
    val imageUrl: String?
) : Parcelable
```

### ⚠️ **MISSING**: Slide-up Bottom Sheet with Details

**What's Missing:**
1. Bottom Sheet UI (should use `BottomSheetDialogFragment`)
2. Layout file for detail view (e.g., `dialog_restaurant_detail.xml`)
3. Display logic for `menu`, `hours`, `reservable` fields

**Current Behavior:** Clicking restaurant opens Google Maps directly

**Expected Behavior:**
1. Click → Slide-up bottom sheet appears
2. Show restaurant details (menu, hours, reservable)
3. "Navigate" button → Opens Google Maps

**To Implement:**
```kotlin
// File: feature/restaurant/src/main/java/com/travelfoodie/feature/restaurant/RestaurantDetailBottomSheet.kt
// Line: NEW FILE NEEDED

class RestaurantDetailBottomSheet(
    private val restaurant: RestaurantEntity
) : BottomSheetDialogFragment() {

    override fun onCreateView(...): View {
        // Inflate dialog_restaurant_detail.xml
        // Display: name, category, rating, distance
        // Display: menu, hours, reservable status
        // Add "Navigate" button → calls openInGoogleMaps()
    }
}
```

---

## 5. Voice Commands (STT/TTS) ⚠️ **NOT IMPLEMENTED**

### 📂 File Structure
```
feature/voice/
└── src/main/java/com/travelfoodie/feature/voice/
    └── (EMPTY - NO FILES)
```

### ❌ **STATUS**: Module exists but completely empty

**Expected Files:**
- `VoiceCommandFragment.kt` - UI with microphone button
- `VoiceCommandViewModel.kt` - STT/TTS logic
- `VoiceCommandParser.kt` - Parse commands like "3월 15일로 변경해줘"

### 🔨 **What Needs to be Implemented:**

#### 1. STT (Speech-to-Text) Integration
```kotlin
// File: feature/voice/src/main/java/com/travelfoodie/feature/voice/VoiceCommandViewModel.kt
// Lines: NEW FILE

class VoiceCommandViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    fun startListening(context: Context) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
        }
        // Launch speech recognizer
    }

    fun processVoiceCommand(text: String, currentTrip: TripEntity) {
        when {
            // "3월 15일로 변경해줘"
            text.contains("변경") && text.contains("월") -> {
                val newDate = parseDateFromText(text)
                updateTripDate(currentTrip, newDate)
            }
            // "서울 추가"
            text.contains("추가") -> {
                val cityName = extractCityName(text)
                addRegionToTrip(currentTrip, cityName)
            }
            // "팀원 추가: 철수"
            text.contains("팀원") && text.contains("추가") -> {
                val memberName = extractMemberName(text)
                addMemberToTrip(currentTrip, memberName)
            }
        }
    }
}
```

#### 2. TTS (Text-to-Speech) for Attractions
```kotlin
// File: feature/voice/src/main/java/com/travelfoodie/feature/voice/TtsHelper.kt
// Lines: NEW FILE

class TtsHelper(context: Context) {
    private val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
        }
    }

    fun speakAttractionDescription(attraction: PoiEntity) {
        val text = "${attraction.name}. ${attraction.description}"
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
```

---

## 6. Home Widget

### 📂 File Structure
```
feature/widget/src/main/java/com/travelfoodie/feature/widget/
└── TripWidgetProvider.kt

feature/widget/src/main/res/layout/
└── widget_trip.xml

app/src/main/AndroidManifest.xml (Lines 56-66: Widget registration)
```

### ✅ **IMPLEMENTED**: Widget Provider Structure

**File:** `feature/widget/src/main/java/com/travelfoodie/feature/widget/TripWidgetProvider.kt`

**Lines 9-36:** Widget provider with hardcoded data
```kotlin
class TripWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // TODO: Load next trip from database
        val views = RemoteViews(context.packageName, R.layout.widget_trip)

        // Update widget views
        views.setTextViewText(R.id.widget_trip_title, "다음 여행")  // ⚠️ Hardcoded
        views.setTextViewText(R.id.widget_trip_dday, "D-5")         // ⚠️ Hardcoded
        views.setTextViewText(R.id.widget_trip_info, "명소 5개 / 맛집 10개")  // ⚠️ Hardcoded

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
```

**File:** `app/src/main/AndroidManifest.xml`

**Lines 56-66:** Widget registration
```xml
<!-- Widget Provider -->
<receiver
    android:name="com.travelfoodie.feature.widget.TripWidgetProvider"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/trip_widget_info" />
</receiver>
```

### ⚠️ **MISSING FEATURES**:

#### 1. Real-time Trip Data Loading
**Problem:** Lines 26-28 use hardcoded strings
**Solution Needed:**
```kotlin
// File: feature/widget/src/main/java/com/travelfoodie/feature/widget/TripWidgetProvider.kt
// Lines 26-28: Replace with database query

private fun updateAppWidget(context: Context, ...) {
    // Query database for next upcoming trip
    val tripRepository = // Inject via WorkManager or manual DI
    val nextTrip = runBlocking { tripRepository.getNextTrip(System.currentTimeMillis()) }

    if (nextTrip != null) {
        // Calculate real D-day
        val daysUntil = TimeUnit.MILLISECONDS.toDays(nextTrip.startDate - System.currentTimeMillis())
        val dDayText = when {
            daysUntil < 0 -> "완료"
            daysUntil == 0L -> "D-Day"
            else -> "D-$daysUntil"
        }

        // Query attraction and restaurant counts
        val attractionCount = poiRepository.getPoiCountForTrip(nextTrip.tripId)
        val restaurantCount = restaurantRepository.getRestaurantCountForTrip(nextTrip.tripId)

        views.setTextViewText(R.id.widget_trip_title, nextTrip.title)
        views.setTextViewText(R.id.widget_trip_dday, dDayText)
        views.setTextViewText(R.id.widget_trip_info, "명소 ${attractionCount}개 / 맛집 ${restaurantCount}개")
    }
}
```

#### 2. Click-to-Navigate PendingIntent
**Missing:** No click handler configured
**Solution Needed:**
```kotlin
// Add after line 28 in TripWidgetProvider.kt

val intent = Intent(context, MainActivity::class.java).apply {
    putExtra("navigate_to", "trip_detail")
    putExtra("trip_id", nextTrip.tripId)
}
val pendingIntent = PendingIntent.getActivity(
    context, 0, intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
views.setOnClickPendingIntent(R.id.widget_trip_container, pendingIntent)
```

---

## 7. Push Notifications (D-7, D-3, D-0)

### 📂 File Structure
```
app/src/main/java/com/travelfoodie/
├── receiver/
│   ├── AlarmReceiver.kt         (Lines 1-75: Notification display logic)
│   └── BootReceiver.kt          (Lines 1-16: Placeholder for rescheduling)
├── service/
│   └── TravelFoodieMessagingService.kt (FCM)
└── TravelFoodieApp.kt           (Lines 33-47: Notification channel)

core/data/src/main/java/com/travelfoodie/core/data/
├── local/entity/Entities.kt    (Lines 148-166: NotifScheduleEntity)
└── repository/TripRepository.kt (Lines 45-68: scheduleNotifications)
```

### ✅ **IMPLEMENTED**: Notification Display Logic

**File:** `app/src/main/java/com/travelfoodie/receiver/AlarmReceiver.kt`

**Lines 15-41:** Receives alarm and shows notification
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    // Support both old and new intent extras format
    val tripTitle = intent.getStringExtra(EXTRA_TRIP_TITLE)
        ?: intent.getStringExtra("trip_title") ?: return
    val notifType = intent.getStringExtra(EXTRA_NOTIF_TYPE)
        ?: intent.getStringExtra("notif_type") ?: return
    val nickname = intent.getStringExtra(EXTRA_NICKNAME)
        ?: intent.getStringExtra("nickname") ?: "여행자"

    val (title, message) = when (notifType) {
        "D-7" -> Pair(
            context.getString(R.string.notif_d7_title),
            context.getString(R.string.notif_d7_message, nickname, tripTitle)
        )
        "D-3" -> Pair(
            context.getString(R.string.notif_d3_title),
            context.getString(R.string.notif_d3_message)
        )
        "D-0" -> Pair(
            context.getString(R.string.notif_d0_title),
            context.getString(R.string.notif_d0_message)
        )
        else -> return
    }

    showNotification(context, title, message)
}
```

**Lines 43-68:** Creates and displays notification
```kotlin
private fun showNotification(context: Context, title: String, message: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, TravelFoodieApp.CHANNEL_TRAVEL_REMINDERS)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
}
```

### ✅ **IMPLEMENTED**: Notification Channel Creation

**File:** `app/src/main/java/com/travelfoodie/TravelFoodieApp.kt`

**Lines 33-47:** Creates notification channel on app start
```kotlin
private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_TRAVEL_REMINDERS,
            "Travel Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for upcoming trips"
            enableVibration(true)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }
}
```

### ⚠️ **PARTIALLY IMPLEMENTED**: Schedule Creation (Database Only)

**File:** `core/data/src/main/java/com/travelfoodie/core/data/repository/TripRepository.kt`

**Lines 45-68:** Creates NotifScheduleEntity records
```kotlin
private suspend fun scheduleNotifications(trip: TripEntity) {
    val schedules = mutableListOf<NotifScheduleEntity>()

    // D-7
    val d7 = trip.startDate - (7 * 24 * 60 * 60 * 1000)
    if (d7 > System.currentTimeMillis()) {
        schedules.add(NotifScheduleEntity(tripId = trip.tripId, fireAt = d7, type = "D-7"))
    }

    // D-3
    val d3 = trip.startDate - (3 * 24 * 60 * 60 * 1000)
    if (d3 > System.currentTimeMillis()) {
        schedules.add(NotifScheduleEntity(tripId = trip.tripId, fireAt = d3, type = "D-3"))
    }

    // D-0
    if (trip.startDate > System.currentTimeMillis()) {
        schedules.add(NotifScheduleEntity(tripId = trip.tripId, fireAt = trip.startDate, type = "D-0"))
    }

    if (schedules.isNotEmpty()) {
        notifScheduleDao.insertSchedules(schedules)  // ✅ Saved to database
    }
}
```

### ❌ **MISSING**: AlarmManager Scheduling

**Problem:** `scheduleNotifications()` only saves to database, doesn't actually schedule alarms!

**Solution Needed:**
```kotlin
// File: core/data/src/main/java/com/travelfoodie/core/data/repository/TripRepository.kt
// Add after line 66 in scheduleNotifications()

private suspend fun scheduleNotifications(trip: TripEntity) {
    // ... existing code ...

    if (schedules.isNotEmpty()) {
        notifScheduleDao.insertSchedules(schedules)

        // ⚠️ ADD THIS: Schedule with AlarmManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        schedules.forEach { schedule ->
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_TRIP_TITLE, trip.title)
                putExtra(AlarmReceiver.EXTRA_NOTIF_TYPE, schedule.type)
                putExtra(AlarmReceiver.EXTRA_NICKNAME, "USER") // Get from auth
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                schedule.scheduleId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                schedule.fireAt,
                pendingIntent
            )
        }
    }
}
```

### ❌ **MISSING**: Boot Receiver Implementation

**File:** `app/src/main/java/com/travelfoodie/receiver/BootReceiver.kt`

**Lines 9-15:** Empty placeholder
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
        // TODO: Reschedule all pending notifications
        // This would require accessing the database to get all NotifScheduleEntity
        // and rescheduling them with AlarmManager
    }
}
```

**Solution Needed:**
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
        // Query database for unsent notification schedules
        CoroutineScope(Dispatchers.IO).launch {
            val schedules = notifScheduleDao.getPendingSchedules(System.currentTimeMillis())
            schedules.forEach { schedule ->
                // Reschedule each alarm with AlarmManager
                // (same logic as in scheduleNotifications above)
            }
        }
    }
}
```

---

## 8. Trip Management (Long-press Edit/Delete)

### 📂 File Structure
```
feature/trip/src/main/java/com/travelfoodie/feature/trip/
├── TripAdapter.kt          (Lines 67-72: Long-press detection)
└── TripListFragment.kt     (Lines 421-488: Edit/Delete dialog)
```

### ✅ **IMPLEMENTED**: Long-press Detection

**File:** `feature/trip/src/main/java/com/travelfoodie/feature/trip/TripAdapter.kt`

**Lines 67-72:** Sets up click and long-click listeners
```kotlin
root.setOnClickListener { onTripClick(trip) }
root.setOnLongClickListener {
    onTripLongClick(trip)  // ✅ Triggers callback
    true
}
```

### ✅ **IMPLEMENTED**: Edit/Delete Menu Dialog

**File:** `feature/trip/src/main/java/com/travelfoodie/feature/trip/TripListFragment.kt`

**Lines 174-177:** Adapter initialization with long-click callback
```kotlin
adapter = TripAdapter(
    onTripClick = { trip -> /* ... */ },
    onTripLongClick = { trip ->
        showTripOptionsDialog(trip)  // ✅ Shows menu
    }
)
```

**Lines 421-488:** Options dialog with Select/Regenerate/Delete
```kotlin
private fun showTripOptionsDialog(trip: TripEntity) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(trip.title)
        .setItems(arrayOf("선택하기", "명소/맛집 재생성", "삭제")) { _, which ->
            when (which) {
                0 -> { /* Select trip */ }
                1 -> { /* Regenerate attractions/restaurants */ }
                2 -> {
                    // Delete trip with confirmation
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("여행 삭제")
                        .setMessage("\"${trip.title}\" 여행을 삭제하시겠습니까?")
                        .setPositiveButton("삭제") { _, _ ->
                            viewModel.deleteTrip(trip)
                            // Show confirmation snackbar
                        }
                        .setNegativeButton("취소", null)
                        .show()
                }
            }
        }
        .show()
}
```

### ✅ **IMPLEMENTED**: D-Day Calculation

**File:** `feature/trip/src/main/java/com/travelfoodie/feature/trip/TripAdapter.kt`

**Lines 45-57:** Real-time D-day calculation
```kotlin
// Calculate dates
val currentTime = System.currentTimeMillis()
val startDate = Date(trip.startDate)
val endDate = Date(trip.endDate)
val daysUntil = TimeUnit.MILLISECONDS.toDays(trip.startDate - currentTime)
val tripDuration = TimeUnit.MILLISECONDS.toDays(trip.endDate - trip.startDate) + 1

// D-Day badge
textViewDDay.text = when {
    daysUntil < 0 -> "완료"      // Past trip
    daysUntil == 0L -> "D-Day"   // Today
    else -> "D-$daysUntil"       // Upcoming trip
}
```

### ⚠️ **MISSING**: Vibration Feedback

**Solution Needed:**
```kotlin
// File: feature/trip/src/main/java/com/travelfoodie/feature/trip/TripAdapter.kt
// Add at line 69 (before onTripLongClick call)

root.setOnLongClickListener {
    // Add vibration feedback
    val vibrator = binding.root.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(50)
    }

    onTripLongClick(trip)
    true
}
```

### ❌ **MISSING**: Swipe-to-Delete

**Solution Needed:**
```kotlin
// File: feature/trip/src/main/java/com/travelfoodie/feature/trip/TripListFragment.kt
// Add in setupRecyclerView() after line 183

val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
    0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    override fun onMove(...) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val trip = adapter.currentList[position]
        viewModel.deleteTrip(trip)

        // Show undo snackbar
        Snackbar.make(binding.root, "\"${trip.title}\" 삭제됨", Snackbar.LENGTH_LONG)
            .setAction("실행취소") {
                viewModel.insertTrip(trip) // Restore
            }
            .show()
    }
})
itemTouchHelper.attachToRecyclerView(binding.recyclerViewTrips)
```

---

## 11. Board/Chat ❌ **NOT IMPLEMENTED**

### 📂 File Structure
```
feature/board/
├── build.gradle.kts  (✅ Dependencies configured)
└── src/main/java/com/travelfoodie/feature/board/
    └── (EMPTY)
```

### ❌ **STATUS**: Zero implementation, only module structure

**What Needs to be Created:**

#### 1. Data Model
```kotlin
// File: core/data/src/main/java/com/travelfoodie/core/data/remote/ChatMessage.kt
// Lines: NEW FILE

data class ChatMessage(
    val messageId: String,
    val tripId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val imageUrl: String? = null,
    val mentions: List<String> = emptyList(),  // User IDs mentioned with @
    val timestamp: Long = System.currentTimeMillis()
)
```

#### 2. Firebase Realtime Database Repository
```kotlin
// File: core/data/src/main/java/com/travelfoodie/core/data/repository/ChatRepository.kt
// Lines: NEW FILE

class ChatRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    fun getChatMessages(tripId: String): Flow<List<ChatMessage>> {
        // Listen to "chats/{tripId}/messages"
    }

    suspend fun sendMessage(message: ChatMessage) {
        database.reference
            .child("chats/${message.tripId}/messages")
            .push()
            .setValue(message)

        // Send FCM notifications to mentioned users
        message.mentions.forEach { userId ->
            sendMentionNotification(userId, message)
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        // Upload to Firebase Storage
        // Return download URL
    }
}
```

#### 3. UI Fragment
```kotlin
// File: feature/board/src/main/java/com/travelfoodie/feature/board/ChatFragment.kt
// Lines: NEW FILE

@AndroidEntryPoint
class ChatFragment : Fragment() {
    private val viewModel: ChatViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()  // Chat messages
        setupInputField()    // Text input with @ mention support
        setupImagePicker()   // Camera/gallery selector
    }

    private fun setupInputField() {
        // Add TextWatcher to detect @ mentions
        // Show autocomplete dropdown with trip members
    }
}
```

---

## 12. OCR Receipt Scanning

### 📂 File Structure
```
app/src/main/java/com/travelfoodie/ocr/
└── ReceiptOcrHelper.kt  (Lines 1-35)

core/data/src/main/java/com/travelfoodie/core/data/local/entity/
└── Entities.kt          (Lines 168-176: ReceiptEntity)
```

### ✅ **IMPLEMENTED**: Basic OCR Text Extraction

**File:** `app/src/main/java/com/travelfoodie/ocr/ReceiptOcrHelper.kt`

**Lines 10-35:** ML Kit text recognition
```kotlin
class ReceiptOcrHelper(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromImage(imageUri: Uri): String {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()

            // Extract text from the result
            val extractedText = result.text

            if (extractedText.isBlank()) {
                "텍스트를 인식할 수 없습니다."
            } else {
                extractedText  // ⚠️ Returns raw text, no parsing
            }
        } catch (e: Exception) {
            throw Exception("OCR 처리 실패: ${e.message}")
        }
    }

    fun close() {
        recognizer.close()
    }
}
```

### ✅ **DATA STRUCTURE**: Receipt Entity

**File:** `core/data/src/main/java/com/travelfoodie/core/data/local/entity/Entities.kt`

**Lines 168-176:** ReceiptEntity
```kotlin
@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey val receiptId: String,
    val restaurantId: String?,  // Can be null until matched
    val merchantName: String,    // ⚠️ Should be extracted from OCR
    val total: Double,           // ⚠️ Should be extracted from OCR
    val imageUrl: String,        // Original receipt photo
    val createdAt: Long = System.currentTimeMillis()
)
```

### ❌ **MISSING**: Merchant Name Extraction

**Solution Needed:**
```kotlin
// File: app/src/main/java/com/travelfoodie/ocr/ReceiptOcrHelper.kt
// Add after line 35

data class ReceiptData(
    val merchantName: String?,
    val total: Double?,
    val rawText: String
)

suspend fun extractReceiptData(imageUri: Uri): ReceiptData {
    val rawText = extractTextFromImage(imageUri)

    // Extract merchant name (usually in first 3 lines)
    val lines = rawText.lines().filter { it.isNotBlank() }
    val merchantName = lines.take(3)
        .firstOrNull { line ->
            // Korean: ends with "점", "마트", "상회" etc
            line.matches(Regex(".*[가-힣]+\\s*(점|마트|상회|식당|음식점|카페)\\s*$"))
        } ?: lines.firstOrNull()

    // Extract total amount
    val total = lines
        .firstOrNull { line ->
            line.contains(Regex("합계|총액|Total|합 계"), ignoreCase = true)
        }?.let { line ->
            // Extract number with optional comma and won symbol
            Regex("[0-9,]+").find(line)?.value?.replace(",", "")?.toDoubleOrNull()
        }

    return ReceiptData(merchantName, total, rawText)
}
```

### ❌ **MISSING**: Auto-matching with Restaurants

**Solution Needed:**
```kotlin
// File: core/data/src/main/java/com/travelfoodie/core/data/repository/ReceiptRepository.kt
// Lines: NEW FILE

class ReceiptRepository @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val restaurantDao: RestaurantDao
) {
    suspend fun findMatchingRestaurant(merchantName: String, userLocation: LatLng?): RestaurantEntity? {
        val allRestaurants = restaurantDao.getAllRestaurants()

        // Fuzzy string matching
        val bestMatch = allRestaurants.maxByOrNull { restaurant ->
            similarityScore(merchantName, restaurant.name)
        }

        return if (bestMatch != null && similarityScore(merchantName, bestMatch.name) > 0.7) {
            bestMatch
        } else null
    }

    private fun similarityScore(s1: String, s2: String): Double {
        // Levenshtein distance or similar algorithm
        // Consider Korean phonetic similarity
    }
}
```

### ❌ **MISSING**: Camera/Gallery UI

**Solution Needed:**
```kotlin
// File: app/src/main/java/com/travelfoodie/ReceiptScanFragment.kt
// Lines: NEW FILE

@AndroidEntryPoint
class ReceiptScanFragment : Fragment() {

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.processReceipt(imageUri)
        }
    }
}
```

---

## Data Flow: Trip Creation → Auto-generation

### Complete Flow with File/Line References

**1. User fills trip form and clicks "Save"**
- **File:** `feature/trip/src/main/java/com/travelfoodie/feature/trip/TripListFragment.kt`
- **Line 274:** Save button onClick listener
- **Line 334:** Calls `viewModel.createTripWithAutoGeneration(trip, region, members, lat, lng)`

**2. ViewModel orchestrates the entire flow**
- **File:** `feature/trip/src/main/java/com/travelfoodie/feature/trip/TripViewModel.kt`
- Sequence:
  1. Insert trip → `tripRepository.insertTrip(trip)`
  2. Insert region → `regionRepository.insertRegion(regionEntity)`
  3. Call OpenAI API → Generate 5 attractions
  4. Insert attractions → `poiRepository.insertPois(poiList)`
  5. Call Kakao/Naver API → Find 10 restaurants
  6. Insert restaurants → `restaurantRepository.insertRestaurants(restaurantList)`
  7. Emit success state with counts

**3. TripRepository schedules notifications**
- **File:** `core/data/src/main/java/com/travelfoodie/core/data/repository/TripRepository.kt`
- **Lines 25-27:** `insertTrip()` calls `scheduleNotifications(trip)`
- **Lines 45-68:** Creates D-7, D-3, D-0 schedule entities
- ⚠️ **Missing:** AlarmManager scheduling (only saves to DB)

**4. SharedViewModel notifies other fragments**
- **File:** `core/ui/src/main/java/com/travelfoodie/core/ui/SharedTripViewModel.kt`
- **Lines 33-38:** `selectTrip(regionId, regionName)` updates StateFlow
- **Result:** RestaurantListFragment and AttractionListFragment observe this and auto-load data

**5. RestaurantListFragment reacts to selection**
- **File:** `feature/restaurant/src/main/java/com/travelfoodie/feature/restaurant/RestaurantListFragment.kt`
- **Lines 50-62:** Observes `sharedViewModel.selectedTripId` (actually regionId)
- **Line 56:** Calls `viewModel.loadRestaurants(tripId)` when selection changes

**6. RestaurantViewModel queries database**
- **File:** `feature/restaurant/src/main/java/com/travelfoodie/feature/restaurant/RestaurantViewModel.kt`
- **Lines 22-28:** `loadRestaurants(regionId)` collects from repository
- **Line 24:** `restaurantRepository.getRestaurantsByRegion(regionId).collect { ... }`

---

## Summary: Implementation Status

| Feature | Implementation Status | File Location |
|---------|----------------------|---------------|
| **Restaurant Google Maps** | ✅ Complete | `RestaurantAdapter.kt:58-76` |
| **Restaurant Detail Popup** | ❌ Missing UI | Need `RestaurantDetailBottomSheet.kt` |
| **Voice Commands (STT)** | ❌ Not Started | `feature/voice/` empty |
| **Voice Commands (TTS)** | ❌ Not Started | Need `TtsHelper.kt` |
| **Widget Structure** | ✅ Complete | `TripWidgetProvider.kt:9-36` |
| **Widget Real Data** | ❌ Hardcoded | Line 26-28 need DB query |
| **Widget Click Handler** | ❌ Missing | Need PendingIntent |
| **Notification Display** | ✅ Complete | `AlarmReceiver.kt:15-68` |
| **Notification Channel** | ✅ Complete | `TravelFoodieApp.kt:33-47` |
| **Notification DB Schedule** | ✅ Complete | `TripRepository.kt:45-68` |
| **Notification AlarmManager** | ❌ Missing | Need AlarmManager.setExact() calls |
| **Boot Receiver Logic** | ❌ Empty | `BootReceiver.kt:9-15` placeholder |
| **Long-press Detection** | ✅ Complete | `TripAdapter.kt:67-72` |
| **Edit/Delete Dialog** | ✅ Complete | `TripListFragment.kt:421-488` |
| **Vibration Feedback** | ❌ Missing | Need Vibrator service call |
| **Swipe-to-Delete** | ❌ Missing | Need ItemTouchHelper |
| **Board/Chat** | ❌ Not Started | `feature/board/` empty |
| **OCR Text Extraction** | ✅ Complete | `ReceiptOcrHelper.kt:14-30` |
| **OCR Merchant Parsing** | ❌ Missing | Need regex extraction |
| **OCR Amount Parsing** | ❌ Missing | Need regex extraction |
| **OCR Auto-matching** | ❌ Missing | Need fuzzy matching algorithm |
| **OCR Camera UI** | ❌ Missing | Need camera fragment |

---

## Build Configuration

### Gradle Files
- **Root:** `build.gradle.kts`
- **App:** `app/build.gradle.kts` - Lines 88-90 (OAuth), 98-101 (Room), 104-110 (Retrofit)
- **Versions:** `gradle/libs.versions.toml` - All dependency versions

### Key Dependencies
- **ML Kit OCR:** Line 25 in `libs.versions.toml` - `mlKit = "16.0.0"`
- **Firebase BOM:** Line 17 - `firebaseBom = "33.2.0"`
- **Hilt:** Line 7 - `hilt = "2.51.1"`
- **Room:** Line 6 - `room = "2.6.1"`

---

## License

MIT License
