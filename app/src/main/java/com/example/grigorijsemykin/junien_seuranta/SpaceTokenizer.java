package com.example.grigorijsemykin.junien_seuranta;

import android.widget.MultiAutoCompleteTextView;

/**
 * Created by Grigorij Semykin on 4.12.2017.
 */

public class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {

    private final char delimiter = ' ';

    public int findTokenStart(CharSequence text, int cursor) {
        int i = cursor;

        while (i > 0 && text.charAt(i - 1) != delimiter) {
            i--;
        }
        while (i < cursor && text.charAt(i) == delimiter) {
            i++;
        }

        return i;
    }

    public int findTokenEnd(CharSequence text, int cursor) {
        int i = cursor;
        int len = text.length();

        while (i < len) {
            if (text.charAt(i) == delimiter) {
                return i;
            } else {
                i++;
            }
        }

        return len;
    }

    public CharSequence terminateToken(CharSequence text) {
        int i = text.length();
        while (i > 0 && text.charAt(i - 1) == delimiter) {
            i--;
        }

        return text;
    }
}
