package com.tata.micalculadora;

/**
 * Created by Tata on 26/09/2017.
 */

public interface OnResolveCallback {
    void onShowMessage(int errorRes); /*Mostrar notificaciones al usuario cuando ocurra un error*/
    void onIsEditing();/* evitar que se produsca algun error dentro del editText*/
}
