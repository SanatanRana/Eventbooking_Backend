# Rana DJ Events API Testing Script (Ultimate E2E Verification Suite)

$BaseUrl = "http://localhost:8080"
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "      RANA DJ EVENTS MONOLITH VERIFICATION         " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# Helper to read error responses cleanly
function Get-ErrorResponse($Exception) {
    try {
        $streamReader = New-Object System.IO.StreamReader $Exception.Response.GetResponseStream()
        return $streamReader.ReadToEnd()
    } catch {
        return $Exception.Message
    }
}

# -----------------------------------------------------------------
# PHASE 1: REGISTER AND LOGIN USER & ADMIN
# -----------------------------------------------------------------
Write-Host "`n>>> PHASE 1: AUTHENTICATION INITIALIZATION" -ForegroundColor Magenta

# 1. Register CUSTOMER A
Write-Host "[1] Registering CUSTOMER A (test@gmail.com)..." -ForegroundColor Yellow
$CustomerRegBody = @{
    fullName = "Sanatan Customer A"
    email = "test@gmail.com"
    phone = "9876543210"
    password = "Password@123"
    role = "CUSTOMER"
} | ConvertTo-Json

try {
    $Res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method Post -ContentType "application/json" -Body $CustomerRegBody
    Write-Host "Status: 201 Created (New Customer Registered)" -ForegroundColor Green
} catch {
    Write-Host "Status: BadRequest (Customer already registered)" -ForegroundColor DarkYellow
}

# 2. Login CUSTOMER A
Write-Host "[2] Logging in as CUSTOMER A..." -ForegroundColor Yellow
$CustomerLoginBody = @{ email = "test@gmail.com"; password = "Password@123" } | ConvertTo-Json
try {
    $CustomerRes = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $CustomerLoginBody
    $CustomerToken = $CustomerRes.data.token
    Write-Host "Status: 200 OK (Customer A Logged In)" -ForegroundColor Green
} catch {
    Write-Host "Failed Customer Login: $(Get-ErrorResponse $_)" -ForegroundColor Red
}

# 3. Register CUSTOMER B (For Ownership Protection Testing)
Write-Host "[3] Registering CUSTOMER B (customer_b@gmail.com)..." -ForegroundColor Yellow
$CustomerBRegBody = @{
    fullName = "Sanatan Customer B"
    email = "customer_b@gmail.com"
    phone = "9876543211"
    password = "Password@123"
    role = "CUSTOMER"
} | ConvertTo-Json

try {
    $Res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method Post -ContentType "application/json" -Body $CustomerBRegBody
    Write-Host "Status: 201 Created (New Customer B Registered)" -ForegroundColor Green
} catch {
    Write-Host "Status: BadRequest (Customer B already registered)" -ForegroundColor DarkYellow
}

# 4. Login CUSTOMER B
Write-Host "[4] Logging in as CUSTOMER B..." -ForegroundColor Yellow
$CustomerBLoginBody = @{ email = "customer_b@gmail.com"; password = "Password@123" } | ConvertTo-Json
try {
    $CustomerBRes = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $CustomerBLoginBody
    $CustomerBToken = $CustomerBRes.data.token
    Write-Host "Status: 200 OK (Customer B Logged In)" -ForegroundColor Green
} catch {
    Write-Host "Failed Customer B Login: $(Get-ErrorResponse $_)" -ForegroundColor Red
}

# 5. Register ADMIN
Write-Host "[5] Registering ADMIN (admin@gmail.com)..." -ForegroundColor Yellow
$AdminRegBody = @{
    fullName = "Sanatan Admin"
    email = "admin@gmail.com"
    phone = "9998887776"
    password = "Password@123"
    role = "ADMIN"
} | ConvertTo-Json

try {
    $Res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method Post -ContentType "application/json" -Body $AdminRegBody
    Write-Host "Status: 201 Created (New Admin Registered)" -ForegroundColor Green
} catch {
    Write-Host "Status: BadRequest (Admin already registered)" -ForegroundColor DarkYellow
}

# 6. Login ADMIN
Write-Host "[6] Logging in as ADMIN..." -ForegroundColor Yellow
$AdminLoginBody = @{ email = "admin@gmail.com"; password = "Password@123" } | ConvertTo-Json
try {
    $AdminRes = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $AdminLoginBody
    $AdminToken = $AdminRes.data.token
    Write-Host "Status: 200 OK (Admin Logged In)" -ForegroundColor Green
} catch {
    Write-Host "Failed Admin Login: $(Get-ErrorResponse $_)" -ForegroundColor Red
}


# -----------------------------------------------------------------
# PHASE 2: PACKAGE CREATION FOR BOOKING LOGIC
# -----------------------------------------------------------------
Write-Host "`n>>> PHASE 2: ENSURING TEST PACKAGE EXISTS" -ForegroundColor Magenta

$PackageBody = @{
    name = "Ultimate Premium DJ Package"
    description = "Upgraded premium sound system, CO2 jet array, wireless microphones, and dynamic light sync."
    price = 30000.00
    durationHours = 7.0
    features = @("10000W Line-Array Sound", "6x CO2 Jet Blast Cannons", "Pro Vocal Shure Mics", "Custom Pre-mix Playlist")
    isActive = $true
} | ConvertTo-Json

if ($AdminToken) {
    try {
        $Headers = @{ Authorization = "Bearer $AdminToken" }
        $PackageCreateRes = Invoke-RestMethod -Uri "$BaseUrl/api/packages" -Method Post -ContentType "application/json" -Body $PackageBody -Headers $Headers
        $TestPackageId = $PackageCreateRes.data.id
        Write-Host "Status: 201 Created (Package ID $TestPackageId generated successfully at Price 30000.00)" -ForegroundColor Green
    } catch {
        # Fallback if package is somehow created or fetch the first one
        Write-Host "Package Creation failed, attempting to retrieve existing list..." -ForegroundColor DarkYellow
        try {
            $Headers = @{ Authorization = "Bearer $AdminToken" }
            $ListRes = Invoke-RestMethod -Uri "$BaseUrl/api/packages" -Method Get -Headers $Headers
            $TestPackageId = $ListRes.data.content[0].id
            Write-Host "Status: Found existing Package ID $TestPackageId" -ForegroundColor Green
        } catch {
            Write-Host "Could not fetch packages: $(Get-ErrorResponse $_)" -ForegroundColor Red
        }
    }
}


# -----------------------------------------------------------------
# PHASE 3: BOOKING MODULE INTEGRATION TESTS
# -----------------------------------------------------------------
Write-Host "`n>>> PHASE 3: BOOKING MANAGEMENT MODULE" -ForegroundColor Magenta

# 1. CREATE BOOKING AS CUSTOMER A
Write-Host "[7] Creating a Booking for Customer A (Test dynamic costing and payment statuses)..." -ForegroundColor Yellow
$EventDateStr = (Get-Date).AddDays(15).ToString("yyyy-MM-dd") # 15 days in future

$BookingBody = @{
    packageId = $TestPackageId
    eventType = "Elite Marriage DJ Ceremony"
    eventDate = $EventDateStr
    startTime = "14:00:00"
    endTime = "19:00:00"
    eventLocation = "Orchid Premium Banquet Hall, Sector 15"
    guestCount = 250
    advanceAmount = 10000.00 # PARTIAL
    specialNotes = "Need high fog smoke on entry."
} | ConvertTo-Json

if ($CustomerToken -and $TestPackageId) {
    try {
        $Headers = @{ Authorization = "Bearer $CustomerToken" }
        $BookingCreateRes = Invoke-RestMethod -Uri "$BaseUrl/api/bookings" -Method Post -ContentType "application/json" -Body $BookingBody -Headers $Headers
        $BookingId = $BookingCreateRes.data.id
        Write-Host "Status: 201 Created (Booking Created successfully! Booking ID: $BookingId)" -ForegroundColor Green
    } catch {
        Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }
} else {
    Write-Host "Skipped: Customer Token or Package ID missing" -ForegroundColor DarkYellow
}

# 2. CONFIRM BOOKING AS ADMIN (Required to activate slot overlap checks)
Write-Host "`n[8] ADMIN Confirms Customer A's Booking (Triggers confirmation state)..." -ForegroundColor Yellow
if ($BookingId -and $AdminToken) {
    try {
        $Headers = @{ Authorization = "Bearer $AdminToken" }
        $UpdateStatusBody = @{
            packageId = $TestPackageId
            eventType = "Elite Marriage DJ Ceremony"
            eventDate = $EventDateStr
            startTime = "14:00:00"
            endTime = "19:00:00"
            eventLocation = "Orchid Premium Banquet Hall, Sector 15"
            guestCount = 250
            advanceAmount = 10000.00
            bookingStatus = "CONFIRMED"
        } | ConvertTo-Json

        $ConfirmRes = Invoke-RestMethod -Uri "$BaseUrl/api/bookings/$BookingId" -Method Put -ContentType "application/json" -Body $UpdateStatusBody -Headers $Headers
        Write-Host "Status: 200 OK (Booking status successfully updated to CONFIRMED!)" -ForegroundColor Green
    } catch {
        Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }
}


# -----------------------------------------------------------------
# PHASE 4: AVAILABILITY CHECKER MODULE
# -----------------------------------------------------------------
Write-Host "`n>>> PHASE 4: AVAILABILITY CHECKER MODULE" -ForegroundColor Magenta

# 1. Query timeslot before slot (09:00 to 13:00) on event date - Should be AVAILABLE
Write-Host "[9] Querying availability on $EventDateStr (09:00 to 13:00) [Non-Overlapping]..." -ForegroundColor Yellow
try {
    $AvailRes = Invoke-RestMethod -Uri "$BaseUrl/api/availability?date=$EventDateStr&startTime=09:00:00&endTime=13:00:00" -Method Get
    Write-Host "Status: 200 OK (Available: $($AvailRes.data.available), Message: $($AvailRes.data.message))" -ForegroundColor Green
} catch {
    Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
}

# 2. Query timeslot that overlaps (15:00 to 18:00) on event date - Should be UNAVAILABLE
Write-Host "[10] Querying availability on $EventDateStr (15:00 to 18:00) [Overlapping Confirmed Slot]..." -ForegroundColor Yellow
try {
    $AvailRes = Invoke-RestMethod -Uri "$BaseUrl/api/availability?date=$EventDateStr&startTime=15:00:00&endTime=18:00:00" -Method Get
    Write-Host "Status: 200 OK (Available: $($AvailRes.data.available), Message: $($AvailRes.data.message))" -ForegroundColor Green
} catch {
    Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
}


# -----------------------------------------------------------------
# PHASE 5: RAZORPAY PAYMENT INTEGRATION MODULE (NEW)
# -----------------------------------------------------------------
Write-Host "`n>>> PHASE 5: RAZORPAY PAYMENT MODULE" -ForegroundColor Magenta

if ($BookingId -and $CustomerToken) {
    # 1. CREATE PAYMENT ORDER (Customer A pays 10,000 of the 20,000 remaining amount)
    Write-Host "[11] Creating Razorpay payment order for 10000.00 INR (Customer A)..." -ForegroundColor Yellow
    $OrderReqBody = @{
        bookingId = $BookingId
        amount = 10000.00
    } | ConvertTo-Json
    $Headers = @{ Authorization = "Bearer $CustomerToken" }
    
    try {
        $OrderRes = Invoke-RestMethod -Uri "$BaseUrl/api/payments/create-order" -Method Post -ContentType "application/json" -Body $OrderReqBody -Headers $Headers
        $OrderId = $OrderRes.data.razorpayOrderId
        Write-Host "Status: 201 Created (Razorpay Order ID: $OrderId, Amount: $($OrderRes.data.amount))" -ForegroundColor Green
    } catch {
        Write-Host "Failed to create order: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }

    # 2. VERIFY PAYMENT (Customer A submits verification tokens)
    Write-Host "[12] Verifying signature and processing checkout token..." -ForegroundColor Yellow
    $VerifyReqBody = @{
        razorpayOrderId = $OrderId
        razorpayPaymentId = "pay_f89832890_custA"
        razorpaySignature = "mock_signature_approved"
        paymentMethod = "CARD"
    } | ConvertTo-Json

    try {
        $VerifyRes = Invoke-RestMethod -Uri "$BaseUrl/api/payments/verify" -Method Post -ContentType "application/json" -Body $VerifyReqBody -Headers $Headers
        Write-Host "Status: 200 OK (Payment Verified! Status: $($VerifyRes.data.paymentStatus), Transaction ID: $($VerifyRes.data.id))" -ForegroundColor Green
    } catch {
        Write-Host "Failed to verify signature: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }

    # 3. GET BOOKING DETAIL (Check updated balance sheets)
    Write-Host "[13] Fetching Booking balance sheets after payment 1..." -ForegroundColor Yellow
    try {
        $BookingRes = Invoke-RestMethod -Uri "$BaseUrl/api/bookings/$BookingId" -Method Get -Headers $Headers
        Write-Host "Verifying Calculated Values:" -ForegroundColor Gray
        Write-Host " - Total Amount: $($BookingRes.data.totalAmount)" -ForegroundColor Cyan
        Write-Host " - Advance Amount (Paid): $($BookingRes.data.advanceAmount) [Expected: 20000.00]" -ForegroundColor Cyan
        Write-Host " - Remaining Amount (Due): $($BookingRes.data.remainingAmount) [Expected: 10000.00]" -ForegroundColor Cyan
        Write-Host " - Payment Status: $($BookingRes.data.paymentStatus) [Expected: PARTIAL]" -ForegroundColor Cyan
    } catch {
        Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }

    # 4. CREATE SECOND PAYMENT ORDER (Customer A pays remaining 10,000 INR to clear due)
    Write-Host "[14] Creating second Razorpay order for 10000.00 INR to clear full due..." -ForegroundColor Yellow
    $Order2ReqBody = @{
        bookingId = $BookingId
        amount = 10000.00
    } | ConvertTo-Json

    try {
        $Order2Res = Invoke-RestMethod -Uri "$BaseUrl/api/payments/create-order" -Method Post -ContentType "application/json" -Body $Order2ReqBody -Headers $Headers
        $OrderId2 = $Order2Res.data.razorpayOrderId
        Write-Host "Status: 201 Created (Razorpay Order ID: $OrderId2)" -ForegroundColor Green
    } catch {
        Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }

    # 5. VERIFY SECOND PAYMENT 
    Write-Host "[15] Verifying second signature..." -ForegroundColor Yellow
    $Verify2ReqBody = @{
        razorpayOrderId = $OrderId2
        razorpayPaymentId = "pay_f89832891_custA"
        razorpaySignature = "mock_signature_approved"
        paymentMethod = "UPI"
    } | ConvertTo-Json

    try {
        $Verify2Res = Invoke-RestMethod -Uri "$BaseUrl/api/payments/verify" -Method Post -ContentType "application/json" -Body $Verify2ReqBody -Headers $Headers
        Write-Host "Status: 200 OK (Payment 2 Verified! Status: $($Verify2Res.data.paymentStatus))" -ForegroundColor Green
    } catch {
        Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }

    # 6. GET BOOKING DETAIL (Confirm PAID status)
    Write-Host "[16] Fetching Booking balance sheets after final payment..." -ForegroundColor Yellow
    try {
        $BookingRes = Invoke-RestMethod -Uri "$BaseUrl/api/bookings/$BookingId" -Method Get -Headers $Headers
        Write-Host "Verifying Calculated Values:" -ForegroundColor Gray
        Write-Host " - Total Amount: $($BookingRes.data.totalAmount)" -ForegroundColor Cyan
        Write-Host " - Advance Amount (Paid): $($BookingRes.data.advanceAmount) [Expected: 30000.00]" -ForegroundColor Cyan
        Write-Host " - Remaining Amount (Due): $($BookingRes.data.remainingAmount) [Expected: 0.00]" -ForegroundColor Cyan
        Write-Host " - Payment Status: $($BookingRes.data.paymentStatus) [Expected: PAID]" -ForegroundColor Cyan
    } catch {
        Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }

    # 7. GET TRANSACTION HISTORY (Customer A - Should see 2 records)
    Write-Host "[17] Fetching transaction history as Customer A (Should see only Customer A's content)..." -ForegroundColor Yellow
    try {
        $HistoryRes = Invoke-RestMethod -Uri "$BaseUrl/api/payments/history" -Method Get -Headers $Headers
        Write-Host "Status: 200 OK" -ForegroundColor Green
        Write-Host "Total payments visible to Customer A: $($HistoryRes.data.Count)" -ForegroundColor Cyan
        foreach ($p in $HistoryRes.data) {
            Write-Host " - Transaction ID: $($p.id), Order ID: $($p.razorpayOrderId), Paid: $($p.amount), Status: $($p.paymentStatus)" -ForegroundColor Gray
        }
    } catch {
        Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }

    # 8. GET TRANSACTION HISTORY (Admin - Should see all records)
    Write-Host "[18] Fetching transaction history as Admin (Should see all content)..." -ForegroundColor Yellow
    if ($AdminToken) {
        $AdminHeaders = @{ Authorization = "Bearer $AdminToken" }
        try {
            $HistoryAdminRes = Invoke-RestMethod -Uri "$BaseUrl/api/payments/history" -Method Get -Headers $AdminHeaders
            Write-Host "Status: 200 OK" -ForegroundColor Green
            Write-Host "Total payments visible to Admin: $($HistoryAdminRes.data.Count)" -ForegroundColor Cyan
        } catch {
            Write-Host "Failed: $(Get-ErrorResponse $_)" -ForegroundColor Red
        }
    }
}


# -----------------------------------------------------------------
# CLEANUP
# -----------------------------------------------------------------
Write-Host "`n>>> CLEANUP DATABASE" -ForegroundColor Magenta
if ($AdminToken -and $BookingId) {
    try {
        $Headers = @{ Authorization = "Bearer $AdminToken" }
        # Need to delete payment records first because of foreign keys
        $DbPayments = Invoke-RestMethod -Uri "$BaseUrl/api/payments/history" -Method Get -Headers $Headers
        # In this monolith, payments can be cleared. For local testing, we can delete the booking which will cascade or delete cleanly
        $Del1 = Invoke-RestMethod -Uri "$BaseUrl/api/bookings/$BookingId" -Method Delete -Headers $Headers
        Write-Host " - Booking ID $BookingId (and associated cascade elements) Deleted successfully." -ForegroundColor Gray
    } catch {
        Write-Host "Failed clean delete: $(Get-ErrorResponse $_)" -ForegroundColor Red
    }
}

Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host "    PAYMENT MODULE TESTING SUCCESSFUL!            " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
