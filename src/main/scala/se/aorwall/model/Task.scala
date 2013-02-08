package se.aorwall.model

import java.util.Date

case class Task (
    val timestamp: Long,
    val title: String,
    val isDone: Boolean
) {

}