package com.example.proyectofinal;

import android.graphics.Rect;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.EditText;

public class PasswordUtils {

    public static void configurarVisibilidad(
            EditText campo
    ) {

        campo.setOnTouchListener((v, event) -> {

            if (event.getAction()
                    == MotionEvent.ACTION_UP) {

                if (campo.getCompoundDrawables()[2]
                        == null) {

                    return false;
                }


                int inicioIcono =
                        campo.getWidth()
                                - campo.getPaddingEnd()
                                - campo.getCompoundDrawables()[2]
                                .getBounds()
                                .width();


                if (event.getX() >= inicioIcono) {

                    int posicionCursor =
                            campo.getSelectionStart();


                    boolean visible =
                            (campo.getInputType()
                                    & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                                    ==
                                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;


                    if (visible) {

                        // OCULTAR CONTRASEÑA
                        campo.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );

                        campo.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_visibility,
                                0
                        );

                    } else {

                        // MOSTRAR CONTRASEÑA
                        campo.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );

                        campo.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_visibility_off,
                                0
                        );
                    }


                    // Mantener cursor al final / misma posición
                    if (posicionCursor >= 0 &&
                            posicionCursor <=
                                    campo.getText().length()) {

                        campo.setSelection(
                                posicionCursor
                        );
                    }


                    return true;
                }
            }


            return false;
        });
    }
}