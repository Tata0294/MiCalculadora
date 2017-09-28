package com.tata.micalculadora;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.etInput)
    EditText etInput;
    @BindView(R.id.contentMain)
    RelativeLayout contentMain;

    private boolean onIsEditInProgress = false;
    private int minLength;
    private int textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        minLength=getResources().getInteger(R.integer.main_min_length);
        textSize=getResources().getInteger(R.integer.main_input_textSize);
        configEditText();
    }

    /****** configuramos para que solo se pueda ingresar la informacion con el teclado de la pantalla ocultando el del dispositivo *****/
    private void configEditText() {
        /*etInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });*/

        etInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_UP){
                    if (motionEvent.getRawX()>=(etInput.getRight()-etInput.getCompoundDrawables()[Constantes.DRAWABLE_RIGHT].getBounds().width())){
                        if (etInput.length()>0){
                            final int length = etInput.getText().length();
                            etInput.getText().delete(length-1,length);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!onIsEditInProgress&& Metodos.canReplaceOperator(charSequence)){
                    onIsEditInProgress = true;
                    etInput.getText().delete(etInput.getText().length()-2,etInput.getText().length()-1);
                }
                //si la longitud actual es mayor a la establecida en el archivo integer correspondiente a la densidad del dispositivo actual
                if (charSequence.length()>minLength){
                    etInput.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize-
                            (((charSequence.length()-minLength)*2)+(charSequence.length()-minLength)));

                //en caso contrario restlablecer el  tamano maximo permitido en la densidad actual
                }else
                    etInput.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                onIsEditInProgress = false;
            }
        });
    }

    /****** Tenemos un arreglo de Id que se estan asociando al metodo onClickNumbers *****/
    @OnClick({R.id.btnSiete, R.id.btnCuatro, R.id.btnUno, R.id.btnPunto, R.id.btnOcho, R.id.btnCinco,
            R.id.btnDos, R.id.btnCero, R.id.btnNueve, R.id.btnSeis, R.id.btnTres})
    public void onClickNumbers(View view) {
        final String valStr = ((Button) view).getText().toString();
        switch (view.getId()) {
            case R.id.btnCero:
            case R.id.btnUno:
            case R.id.btnDos:
            case R.id.btnTres:
            case R.id.btnCuatro:
            case R.id.btnCinco:
            case R.id.btnSeis:
            case R.id.btnSiete:
            case R.id.btnOcho:
            case R.id.btnNueve:
                //El metodo append sirve para anadir texto a nuestro editText
                etInput.getText().append(valStr);
                break;
            case R.id.btnPunto:
                //Le estamos diciendo que de nuestro editText tome el texto en formato string y lo asigne a la variable operacion
                final String operacion = etInput.getText().toString();
                final String operador = Metodos.getOperator(operacion);
                final int count = operacion.length() - operacion.replace(".", "").length();

                if (!operacion.contains(Constantes.POINT) || count < 2 && (!operador.equals(Constantes.OPERATOR_NULL))) {
                    etInput.getText().append(valStr);
                }
                break;
        }
    }

    @OnClick({R.id.btnLimpiar, R.id.btnDiv, R.id.btnMul, R.id.btnResta, R.id.btnSuma, R.id.btnIgual})
    public void onClickControl(View view) {
        switch (view.getId()) {
            case R.id.btnLimpiar:
                //remplaza el valor por el que le estamos mandando
                etInput.setText("");
                break;
            case R.id.btnSuma:
            case R.id.btnResta:
            case R.id.btnMul:
            case R.id.btnDiv:
                resolve(false);
                final String operador = ((Button)view).getText().toString();
                final String operacion = etInput.getText().toString();

                final String ultimoCaracter = operacion.isEmpty()?"":operacion.substring(operacion.length()-1);

                if(operador.equals((Constantes.OPERATOR_SUB))){
                    if (operacion.isEmpty() ||
                            (!(ultimoCaracter.equals(Constantes.OPERATOR_SUB))&&
                            !(ultimoCaracter.equals(Constantes.POINT)))){
                        etInput.getText().append(operador);
                    }
                }else{
                    if(!operacion.isEmpty() &&
                            !(ultimoCaracter.equals(Constantes.OPERATOR_SUB))&&
                            !(ultimoCaracter.equals(Constantes.POINT))){
                        etInput.getText().append(operador);
                    }
                }
                break;
            case R.id.btnIgual:
                resolve(true);
                break;
        }
    }

    private void resolve(boolean fromResult) {
        Metodos.tryResolve(fromResult, etInput, new OnResolveCallback() {
            @Override
            public void onShowMessage(int errorRes) {
                showMessage(errorRes);
            }

            @Override
            public void onIsEditing() {
                onIsEditInProgress=true;
            }
        });

    }

    private void showMessage(int errorRes) {
        Snackbar.make(contentMain,errorRes,Snackbar.LENGTH_SHORT).show();
    }
}








