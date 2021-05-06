package com.example.aimsandroid.utils

import com.example.aimsandroid.databinding.FormDeliveryBinding
import com.example.aimsandroid.databinding.FormPickupBinding
import com.google.android.material.snackbar.Snackbar
import java.lang.StringBuilder

/**
 * This class contains methods to validate form input
 */
fun validateDeliveryForm(formDeliveryBinding: FormDeliveryBinding): Boolean {
    var valid = true
    if(formDeliveryBinding.initialFuelStickReading.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.initialFuelStickReading.error = "Required"
    }

    if(formDeliveryBinding.finalFuelStickReading.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.finalFuelStickReading.error = "Required"
    }

    if(formDeliveryBinding.productDropped.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.productDropped.error = "Required"
    }

    if(formDeliveryBinding.deliveryStarted.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.deliveryStarted.error = "Required"
    }

    if(formDeliveryBinding.deliveryEnded.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.deliveryEnded.error = "Required"
    }

    if(formDeliveryBinding.grossQuantity.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.grossQuantity.error = "Required"
    }

    if(formDeliveryBinding.netQuantity.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.netQuantity.error = "Required"
    }

    if(formDeliveryBinding.deliveryTicketNumber.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.deliveryTicketNumber.error = "Required"
    }

    if(formDeliveryBinding.billOfLadingNumber.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.billOfLadingNumber.error = "Required"
    }

    if(formDeliveryBinding.pickedUpBy.text.toString().isEmpty()){
        valid = false
        formDeliveryBinding.pickedUpBy.error = "Required"
    }
    return valid
}

fun validatePickUpForm(formPickupBinding: FormPickupBinding): Boolean {
    var valid = true
    if(formPickupBinding.initialFuelStickReading.text.toString().isEmpty()){
        valid = false
        formPickupBinding.initialFuelStickReading.error = "Required"
    }

    if(formPickupBinding.finalFuelStickReading.text.toString().isEmpty()){
        valid = false
        formPickupBinding.finalFuelStickReading.error = "Required"
    }

    if(formPickupBinding.productPickedUp.text.toString().isEmpty()){
        valid = false
        formPickupBinding.productPickedUp.error = "Required"
    }

    if(formPickupBinding.pickupStarted.text.toString().isEmpty()){
        valid = false
        formPickupBinding.pickupStarted.error = "Required"
    }

    if(formPickupBinding.pickupEnded.text.toString().isEmpty()){
        valid = false
        formPickupBinding.pickupEnded.error = "Required"
    }

    if(formPickupBinding.grossQuantity.text.toString().isEmpty()){
        valid = false
        formPickupBinding.grossQuantity.error = "Required"
    }

    if(formPickupBinding.netQuantity.text.toString().isEmpty()){
        valid = false
        formPickupBinding.netQuantity.error = "Required"
    }

    if(formPickupBinding.pickupTicketNumber.text.toString().isEmpty()){
        valid = false
        formPickupBinding.pickupTicketNumber.error = "Required"
    }

    if(formPickupBinding.billOfLadingNumber.text.toString().isEmpty()){
        valid = false
        formPickupBinding.billOfLadingNumber.error = "Required"
    }

    if(formPickupBinding.pickedUpBy.text.toString().isEmpty()){
        valid = false
        formPickupBinding.pickedUpBy.error = "Required"
    }
    return valid
}

fun getDeliveryFormSummary(formDeliveryBinding: FormDeliveryBinding): String {
    val sb = StringBuilder("")
    sb.append("\nInitial Fuel Stick Reading: ")
    sb.append(formDeliveryBinding.initialFuelStickReading.text.toString())
    sb.append("\nFinal Fuel Stick Reading: ")
    sb.append(formDeliveryBinding.finalFuelStickReading.text.toString())
    sb.append("\nProduct Delivered: ")
    sb.append(formDeliveryBinding.productDropped.text.toString())
    sb.append("\nDelivery Started: ")
    sb.append(formDeliveryBinding.deliveryStarted.text.toString())
    sb.append("\nDelivery Ended: ")
    sb.append(formDeliveryBinding.deliveryEnded.text.toString())
    sb.append("\nGross Quantity: ")
    sb.append(formDeliveryBinding.grossQuantity.text.toString())
    sb.append("\nNet Quantity: ")
    sb.append(formDeliveryBinding.netQuantity.text.toString())
    sb.append("\nDelivery Ticket Number: ")
    sb.append(formDeliveryBinding.deliveryTicketNumber.text.toString())
    sb.append("\nBill of Lading Number: ")
    sb.append(formDeliveryBinding.billOfLadingNumber.text.toString())
    sb.append("\nReceived by: ")
    sb.append(formDeliveryBinding.pickedUpBy.text.toString())
    sb.append("\nComments: ")
    sb.append(formDeliveryBinding.comment.text.toString())
    return sb.toString()
}

fun getPickUpFormSummary(formPickupBinding: FormPickupBinding): String {
    val sb = StringBuilder("")
    sb.append("\nInitial Fuel Stick Reading: ")
    sb.append(formPickupBinding.initialFuelStickReading.text.toString())
    sb.append("\nFinal Fuel Stick Reading: ")
    sb.append(formPickupBinding.finalFuelStickReading.text.toString())
    sb.append("\nProduct Delivered: ")
    sb.append(formPickupBinding.productPickedUp.text.toString())
    sb.append("\nDelivery Started: ")
    sb.append(formPickupBinding.pickupStarted.text.toString())
    sb.append("\nDelivery Ended: ")
    sb.append(formPickupBinding.pickupEnded.text.toString())
    sb.append("\nGross Quantity: ")
    sb.append(formPickupBinding.grossQuantity.text.toString())
    sb.append("\nNet Quantity: ")
    sb.append(formPickupBinding.netQuantity.text.toString())
    sb.append("\nDelivery Ticket Number: ")
    sb.append(formPickupBinding.pickupTicketNumber.text.toString())
    sb.append("\nBill of Lading Number: ")
    sb.append(formPickupBinding.billOfLadingNumber.text.toString())
    sb.append("\nReceived by: ")
    sb.append(formPickupBinding.pickedUpBy.text.toString())
    sb.append("\nComments: ")
    sb.append(formPickupBinding.comment.text.toString())
    return sb.toString()
}