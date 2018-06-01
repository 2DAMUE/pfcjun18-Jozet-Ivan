package com.quadram.futh.helper;

public class Constantes {
    public static final String SHOW_NOTIFICATION = "show_notification";
    public static final String READ_ACTION = "com.quadram.futh.ACTION_MESSAGE_READ";
    public static final String REPLY_ACTION = "com.quadram.futh.ACTION_MESSAGE_REPLY";
    public static final String NOTIFICATION_ID_STRING = "notification_id";
    public static final String VOICE_REPLY = "voice_reply";
    public static final int NOTIFICATION_ID_INT = 100;
    public static final String CHANNEL_GAS = "Gas";
    public static final String CHANNEL_HUMIDITY = "Humidity";
    public static final String CHANNEL_LIGHT = "Light";
    public static final String CHANNEL_PLUG = "Plug";
    public static final String CHANNEL_TEMPERATURE = "Temperature";

    // COMANDOS DE VOZ PARA NOTIFICACIONES DE LUZ
    public static final String[] COMANDOS_ENCENDER_LUZ = {
            "Enciende la de nuevo",
            "Enciéndeme la de nuevo",
            "Enciende la luz de nuevo",
            "Enciende la luz ahora mismo",
            "Yo no he dado permiso para que apaguen la luz",
            "Yo no he dado permiso para que se apague la luz",
            "Yo no he dado permiso para que apague la luz",
            "Encender"
    };
    public static final String[] COMANDOS_APAGAR_LUZ = {
            "Apaga la de nuevo",
            "Apágame la de nuevo",
            "Apaga la luz de nuevo",
            "Apaga la luz ahora mismo",
            "Yo no he dado permiso para que enciendan la luz",
            "Yo no he dado permiso para que se encienda la luz",
            "Yo no he dado permiso para que encienda la luz",
            "Apagar"
    };

    // COMANDOS DE VOZ PARA NOTIFICACIONES DE ENCHUFE
    public static final String[] COMANDOS_ACTIVAR_ENCHUFE = {
            "Activa lo de nuevo",
            "Actívame lo de nuevo",
            "Activa el enchufe de nuevo",
            "Activa el enchufe ahora mismo",
            "Yo no he dado permiso para que desactiven el enchufe",
            "Yo no he dado permiso para que se desactive el enchufe",
            "Yo no he dado permiso para que desactive el enchufe",
            "Activar"
    };
    public static final String[] COMANDOS_DESACTIVAR_ENCHUFE = {
            "Desactiva lo de nuevo",
            "Desactívame lo de nuevo",
            "Desactiva el enchufe de nuevo",
            "Desactiva el enchufe ahora mismo",
            "Yo no he dado permiso para que activen el enchufe",
            "Yo no he dado permiso para que se active el enchufe",
            "Yo no he dado permiso para que active el enchufe",
            "Desactivar"
    };

    // COMANDOS DE VOZ PARA NOTIFICACIONES DE GAS
    public static final String[] COMANDOS_ESTADO_GAS = {
            "Muéstrame el estado del gas",
            "Ver el estado del gas",
            "Qué nivel de gas hay en mi casa",
            "En qué nivel está",
            "En qué nivel está el gas",
            "En qué nivel de riesgo está el gas",
            "Qué nivel de riesgo hay"
    };

    // COMANDOS DE VOZ PARA NOTIFICACIONES DE HUMEDAD
    public static final String[] COMANDOS_VALOR_HUMEDAD = {
            "Qué humedad hace en mi casa",
            "Qué humedad hace en casa",
            "Qué humedad hace",
            "Muéstrame la humedad que hace en mi casa",
            "Muéstrame la humedad que hace en casa",
            "Muéstrame la humedad que hace",
            "Muéstrame la humedad",
            "Dime la humedad que hace en mi casa",
            "Dime la humedad que hace en casa",
            "Dime la humedad que hace",
            "Dime la humedad"
    };

    // COMANDOS DE VOZ PARA NOTIFICACIONES DE TEMPERATURA
    public static final String[] COMANDOS_VALOR_TEMPERATURA = {
            "Qué temperatura hace en mi casa",
            "Qué temperatura hace en casa",
            "Qué temperatura hace",
            "Cuál es la temperatura que hace en mi casa",
            "Cuál es la temperatura que hace en casa",
            "Cuál es la temperatura que hace",
            "Cuál es la temperatura",
            "Muéstrame la temperatura que hace en mi casa",
            "Muéstrame la temperatura que hace en casa",
            "Muéstrame la temperatura que hace",
            "Muéstrame la temperatura",
            "Dime la temperatura que hace en mi casa",
            "Dime la temperatura que hace en casa",
            "Dime la temperatura que hace",
            "Dime la temperatura"
    };
}
