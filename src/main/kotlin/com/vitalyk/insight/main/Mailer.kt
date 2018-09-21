package com.vitalyk.insight.main

import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun sendEmail(to: String, subject: String, content: String, isHtml: Boolean = false) {
    val smtpServer = "smtp.gmail.com"
    val port = 465
    val username = "vitalyx@gmail.com"
    val password = "kcoomuicctzglszn"
    val from = "insight@vitalyk.com"
    val bounceAddress = "vitalyx@gmail.com"

    val props = mapOf(
        "mail.smtp.auth" to "true",
        "mail.smtp.host" to smtpServer,
        "mail.transport.protocol" to "smtp",
        "mail.smtp.from" to bounceAddress,
        // SSL
        "mail.smtp.socketFactory.port" to port.toString(),
        "mail.smtp.socketFactory.class" to "javax.net.ssl.SSLSocketFactory",
        "mail.smtp.socketFactory.fallback" to "true"
        // TLS
        // "mail.smtp.starttls.enable" to "true",
        // "mail.smtp.port" to "587"
    ).toProperties()

    val authenticator = object : Authenticator() {
        override fun getPasswordAuthentication() = PasswordAuthentication(username, password)
    }
    val session = Session.getInstance(props, authenticator)
    val message = MimeMessage(session).apply {
        addFrom(InternetAddress.parse(from))
        setRecipients(Message.RecipientType.TO, to)
        setSubject(subject.substringBefore('\n'))

        if (isHtml) {
            setContent(content, "text/html; charset=utf-8")
        } else {
            setText(content, "utf-8")
        }

        saveChanges()
    }

    val transport = session.transport
    try {
        transport.connect(smtpServer, port, username, password)
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    transport.close()
}