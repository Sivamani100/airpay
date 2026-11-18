package com.arkiolabs.downpay.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class BankSmsParserTest : FunSpec({

    test("parse SBI debit SMS with UPI Ref") {
        val sender = "VM-SBIUPI"
        val body = "Dear UPI user A/C X7821 debited by 500.00 on 18Nov26 transfer to 9876543210 Ref No 632412890123. Bal Rs 12450.50 -SBI"
        
        val parsed = BankSmsParser.parse(sender, body)
        parsed shouldNotBe null
        parsed?.isSuccess shouldBe true
        parsed?.isDebit shouldBe true
        parsed?.amount shouldBe "500.00"
        parsed?.recipient shouldBe "9876543210"
        parsed?.refId shouldBe "632412890123"
        parsed?.balance shouldBe "12450.50"
        parsed?.bankName shouldBe "State Bank of India"
    }

    test("parse HDFC debit SMS with VPA") {
        val sender = "AD-HDFCBK"
        val body = "Rs 210.00 debited from HDFC Bank A/c **4120 on 18-NOV-26 towards VPA merchant@okhdfcbank UPI Ref: 332211445566. Avl bal: Rs 4530.00"
        
        val parsed = BankSmsParser.parse(sender, body)
        parsed shouldNotBe null
        parsed?.isSuccess shouldBe true
        parsed?.amount shouldBe "210.00"
        parsed?.refId shouldBe "332211445566"
        parsed?.balance shouldBe "4530.00"
        parsed?.bankName shouldBe "HDFC Bank"
    }

    test("parse ICICI debit SMS with UTR") {
        val sender = "VK-ICICIB"
        val body = "Your A/C 9901 has been debited for INR 1500.00 on 18-Nov-26. UTR: 445566778899. Available Balance is INR 8400.25."
        
        val parsed = BankSmsParser.parse(sender, body)
        parsed shouldNotBe null
        parsed?.isSuccess shouldBe true
        parsed?.amount shouldBe "1500.00"
        parsed?.refId shouldBe "445566778899"
        parsed?.balance shouldBe "8400.25"
        parsed?.bankName shouldBe "ICICI Bank"
    }

    test("parse failed transaction SMS") {
        val sender = "AXISBK"
        val body = "Your transaction of Rs 500.00 failed due to incorrect UPI PIN. Available balance is Rs 3400.00"
        
        val parsed = BankSmsParser.parse(sender, body)
        parsed shouldNotBe null
        parsed?.isSuccess shouldBe false
        parsed?.bankName shouldBe "Axis Bank"
    }

    test("parse balance check SMS") {
        val sender = "UnionBank"
        val body = "Dear Customer, Available Balance in A/c ending 1234 is INR 24,560.00 as on 18-Nov-2026."
        
        val parsed = BankSmsParser.parse(sender, body)
        parsed shouldNotBe null
        parsed?.isSuccess shouldBe true
        parsed?.balance shouldBe "24560.00"
        parsed?.bankName shouldBe "Union Bank of India"
    }
})
