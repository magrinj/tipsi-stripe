package com.gettipsi.stripe;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.CardNumberEditText;
import com.stripe.android.view.ExpiryDateEditText;
import com.stripe.android.view.StripeEditText;
import com.stripe.android.model.Card;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by dmitriy on 11/15/16
 */

public class CustomCardInputReactManager extends SimpleViewManager<CardInputWidget> {

  public static final String REACT_CLASS = "CardInputWidget";
  private static final String TAG = CustomCardInputReactManager.class.getSimpleName();
  private static final String NUMBER = "number";
  private static final String EXP_MONTH = "expMonth";
  private static final String EXP_YEAR = "expYear";
  private static final String CCV = "cvc";

  private ThemedReactContext reactContext;
  private WritableMap currentParams;

  private String EXP_DATE_SEPARATOR = "/";
  private String currentNumber;
  private int currentMonth;
  private int currentYear;
  private String currentCCV;

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  protected CardInputWidget createViewInstance(ThemedReactContext reactContext) {
    XmlPullParser parser = reactContext.getResources().getXml(R.xml.stripe_card_widget);
    try {
      parser.next();
      parser.nextTag();
    } catch (Exception e) {
      e.printStackTrace();
    }

    AttributeSet attr = Xml.asAttributeSet(parser);
    final CardInputWidget creditCardForm = new CardInputWidget(reactContext, attr);
    setListeners(creditCardForm);
    this.reactContext = reactContext;
    return creditCardForm;
  }

  @ReactProp(name = "enabled")
  public void setEnabled(CardInputWidget view, boolean enabled) {
    view.setEnabled(enabled);
  }

  @ReactProp(name = "backgroundColor")
  public void setBackgroundColor(CardInputWidget view, int color) {
    Log.d("TAG", "setBackgroundColor: "+color);
    view.setBackgroundColor(color);
  }

  @ReactProp(name = "cardNumber")
  public void setCardNumber(CardInputWidget view, String cardNumber) {
    view.setCardNumber(cardNumber);
  }

  @ReactProp(name = "expiryDate")
  public void setExpDate(CardInputWidget view, String expiryDate) {
    String[] parts = expiryDate.split(EXP_DATE_SEPARATOR);

    if (parts.length  == 2) {
      try {
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);

        view.setExpiryDate(month, year);
      } catch (NumberFormatException e) {
      }
    }
  }

  @ReactProp(name = "cvcCode")
  public void setCvcCode(CardInputWidget view, String cvcCode) {
    view.setCvcCode(cvcCode);
  }

  @ReactProp(name = "numberPlaceholder")
  public void setCreditCardTextHint(CardInputWidget view, String creditCardTextHint) {
    final CardNumberEditText ccNumberEdit = (CardNumberEditText) view.findViewById(R.id.et_card_number);

    ccNumberEdit.setHint(creditCardTextHint);
  }

  @ReactProp(name = "expirationPlaceholder")
  public void setExpDateTextHint(CardInputWidget view, String expDateTextHint) {
    final ExpiryDateEditText ccExpEdit = (ExpiryDateEditText) view.findViewById(R.id.et_expiry_date);

    ccExpEdit.setHint(expDateTextHint);
  }

  @ReactProp(name = "cvcPlaceholder")
  public void setSecurityCodeTextHint(CardInputWidget view, String securityCodeTextHint) {
    final StripeEditText ccCcvEdit = (StripeEditText) view.findViewById(R.id.et_cvc_number);

    ccCcvEdit.setHint(securityCodeTextHint);
  }

  private void setListeners(final CardInputWidget view){

    final CardNumberEditText ccNumberEdit = (CardNumberEditText) view.findViewById(R.id.et_card_number);
    final ExpiryDateEditText ccExpEdit = (ExpiryDateEditText) view.findViewById(R.id.et_expiry_date);
    final StripeEditText ccCcvEdit = (StripeEditText) view.findViewById(R.id.et_cvc_number);

    ccNumberEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.d(TAG, "onTextChanged: cardNumber = "+charSequence);
        currentNumber = charSequence.toString().replaceAll(" ", "");
        postEvent(view);
      }

      @Override
      public void afterTextChanged(Editable editable) {
      }
    });

    ccExpEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String expDate = charSequence.toString();
        String[] parts = expDate.split(EXP_DATE_SEPARATOR);

        Log.d(TAG, "onTextChanged: EXP_YEAR = "+charSequence);

        try {
          if (parts.length > 0) {
            currentMonth = Integer.parseInt(parts[0]);
          }

          if (parts.length > 1) {
            currentYear = Integer.parseInt(parts[1]);
          }

          postEvent(view);
        } catch (NumberFormatException e) {
        }
      }

      @Override
      public void afterTextChanged(Editable editable) {
      }
    });

    ccCcvEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.d(TAG, "onTextChanged: CCV = "+charSequence);
        currentCCV = charSequence.toString();
        postEvent(view);
      }

      @Override
      public void afterTextChanged(Editable editable) {
      }
    });
  }

  private void postEvent(CardInputWidget view){
    Card card = view.getCard();
    boolean isValidCard = (card != null ? card.validateCard() : false);

    currentParams = Arguments.createMap();
    currentParams.putString(NUMBER, currentNumber);
    currentParams.putInt(EXP_MONTH, currentMonth);
    currentParams.putInt(EXP_YEAR, currentYear);
    currentParams.putString(CCV, currentCCV);
    reactContext.getNativeModule(UIManagerModule.class)
      .getEventDispatcher().dispatchEvent(
      new CreditCardFormOnChangeEvent(view.getId(), currentParams, isValidCard));
  }

  private void updateView(CardInputWidget view){

  }
}
