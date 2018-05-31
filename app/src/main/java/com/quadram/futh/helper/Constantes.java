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
            "Yo no he dado permiso para que enciendan la luz",
            "Yo no he dado permiso para que se encienda la luz",
            "Yo no he dado permiso para que encienda la luz"
    };
    public static final String[] COMANDOS_APAGAR_LUZ = {
            "Apaga la de nuevo",
            "Apágame la de nuevo",
            "Apaga la luz de nuevo",
            "Apaga la luz ahora mismo",
            "Yo no he dado permiso para que apaguen la luz",
            "Yo no he dado permiso para que se apague la luz",
            "Yo no he dado permiso para que apague la luz"
    };

    // COMANDOS DE VOZ PARA NOTIFICACIONES DE ENCHUFE
    public static final String[] COMANDOS_ACTIVAR_ENCHUFE = {
            "Enciende la de nuevo",
            "Enciéndeme la de nuevo",
            "Enciende la luz de nuevo",
            "Enciende la luz ahora mismo",
            "Yo no he dado permiso para que enciendan la luz",
            "Yo no he dado permiso para que se encienda la luz",
            "Yo no he dado permiso para que encienda la luz"
    };
    public static final String[] COMANDOS_DESACTIVAR_ENCHUFE = {
            "Apaga la de nuevo",
            "Apágame la de nuevo",
            "Apaga la luz de nuevo",
            "Apaga la luz ahora mismo",
            "Yo no he dado permiso para que apaguen la luz",
            "Yo no he dado permiso para que se apague la luz",
            "Yo no he dado permiso para que apague la luz"
    };
}
