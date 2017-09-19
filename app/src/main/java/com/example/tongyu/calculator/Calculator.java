package com.example.tongyu.calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Calculator extends AppCompatActivity
{
    private TextView screen;
    private String currentStr;
    private String previousInput; // for case when you press =, then press number

    private List<String> infix = new LinkedList<>();
    private List<String> postfix = new LinkedList<>();
    private Stack<String> stack = new Stack<>();
    private Stack<BigDecimal> postfixStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator);
        initializeComponent();
    }

    public void initializeComponent()
    {
        screen = (TextView) findViewById(R.id.screen);
        currentStr = "0";
        previousInput = "";
    }

    // number done
    public void onClickNumber(View v)
    {
        Button b = (Button) v;
        String inputNumber = b.getText().toString();

        if(lengthIsOK())
        {
            if(!previousInput.equals("="))
            {
                if(currentStr.length() > 2)
                {
                    char lastChar = currentStr.charAt(currentStr.length() - 1);
                    char secondLastChar = currentStr.charAt(currentStr.length() - 2);

                    // if last number is 0 and second last character is operand, example, 3+0, 4-0
                    if(lastChar == '0' && (secondLastChar == '\uFF0B' || secondLastChar == '\u2014' || secondLastChar == '*' || secondLastChar == '/' || secondLastChar == '-'))
                    {
                        currentStr = currentStr.substring(0, currentStr.length()-1) + inputNumber;
                    }
                    else
                    {
                        currentStr += inputNumber;
                    }
                }
                else if (currentStr.equals("0"))
                {
                    currentStr = inputNumber;
                }
                else if (currentStr.equals("-0"))
                {
                    currentStr = "-" + inputNumber;
                }
                else
                {
                    currentStr += inputNumber;
                }
            }
            else
            {
                currentStr = inputNumber;
            }
        }

        //
        screen.setText(currentStr);
        previousInput = inputNumber;
    }

    // operand done
    public void onClickOperand(View v)
    {
        Button b = (Button) v;
        String inputOperand = b.getText().toString();
        char lastChar = currentStr.charAt(currentStr.length() - 1);

        //+,-,*,/,. \u2014 stands em-dash
        if(lastChar == '\uFF0B' || lastChar == '\u2014' || lastChar == '*' || lastChar == '/' || lastChar == '.')
        {
            currentStr = currentStr.substring(0, currentStr.length()-1) + inputOperand;
        }
        else
        {
            currentStr += inputOperand;
        }

        //
        screen.setText(currentStr);
        previousInput = inputOperand;
    }

    public void onClickEqual(View v)
    {
        // since only way to stopping the program is pressing equal sign, try catch to avoid stopping program
        try
        {
            shuntingYard();
            evaluatePostfix();
        }
        catch (Exception e)
        {
            if (e instanceof ArithmeticException)
            {
            }
            else
            {
                clear();
                screen.setText("Exception");
            }
        }
        previousInput = "=";
    }

    // done
    public void onClickDot(View v)
    {
        Button b = (Button) v;
        char lastChar = currentStr.charAt(currentStr.length() - 1);
        boolean hasDot = false;

        // case : last character is +, -, *, /
        if(lastChar == '\uFF0B' || lastChar == '\u2014' || lastChar == '*' || lastChar == '/')
        {
            currentStr += "0.";
        }
        else
        {
            for(int i = currentStr.length()-1; i >= 0; --i)
            {
                // if you find a dot
                if(currentStr.charAt(i) == '.')
                {
                    hasDot = true;
                    break;
                }
                // if you find a operand before a dot
                if(currentStr.charAt(i) == '\uFF0B' || currentStr.charAt(i) == '\u2014' || currentStr.charAt(i) == '*' || currentStr.charAt(i) == '/')
                {
                    hasDot = false;
                    break;
                }
                // search til end, example 0, 777
                if(i == 0)
                {
                    hasDot = false;
                }
            }
            if(!hasDot)
            {
                currentStr += ".";
            }
        }

        previousInput = b.getText().toString();
        screen.setText(currentStr);
    }

    // done
    public void onClickNegate(View v)
    {
        char lastChar = currentStr.charAt(currentStr.length() - 1);
        // do nothing
        if(lastChar == '\uFF0B' || lastChar == '\u2014' || lastChar == '*' || lastChar == '/')
        {
        }
        else
        {
            for(int i = currentStr.length()-1; i >= 0; --i)
            {
                // if found -, get rid of it
                if(currentStr.charAt(i) == '-')
                {
                    if(i == 0)
                        currentStr = currentStr.substring(1);
                    else
                        currentStr = currentStr.substring(0, i) + currentStr.substring(i+1, currentStr.length());
                    break;
                }
                // if found +,  /u2014, *, / good
                else if(currentStr.charAt(i) == '\uFF0B' || currentStr.charAt(i) == '\u2014' || currentStr.charAt(i) == '*' || currentStr.charAt(i) == '/')
                {
                    currentStr = currentStr.substring(0, i+1) + "-" + currentStr.substring(i+1, currentStr.length());
                    break;
                }
                // add - to beginning
                else if(i == 0)
                {
                    currentStr = "-" + currentStr;
                    break;
                }
            }
        }
        screen.setText(currentStr);
        previousInput = "-";
    }

    // done
    public void onClickBackSpace(View v)
    {
        if(currentStr.length() == 1)
        {
            currentStr = "0";
        }
        else
        {
            char secondLastChar = currentStr.charAt(currentStr.length() - 2);
            if(currentStr.length() == 2 && secondLastChar == '-')
            {
                currentStr = "0";
            }
            else
            {
                currentStr = currentStr.substring(0, currentStr.length()-1);
                if(currentStr.charAt(currentStr.length() -1) == '-')
                    currentStr = currentStr.substring(0, currentStr.length()-1);
            }
        }

        screen.setText(currentStr);
        previousInput = "B";
    }

    public void onClickClear(View v)
    {
        clear();
        previousInput = "C";
    }

    public void clear()
    {
        currentStr = "0";
        infix.clear();
        postfix.clear();
        stack.clear();
        postfixStack.clear();
        screen.setText(currentStr);
    }

    // limit each number input digit to 9
    public boolean lengthIsOK()
    {
        char lastChar = currentStr.charAt(currentStr.length() - 1);
        // do nothing
        if(lastChar == '\uFF0B' || lastChar == '\u2014' || lastChar == '*' || lastChar == '/')
        {
            return true;
        }
        else
        {
            int count = 0;
            for(int i = currentStr.length() - 1;  i >= 0; --i)
            {
                if(currentStr.charAt(i) == '\uFF0B' || currentStr.charAt(i) == '\u2014' || currentStr.charAt(i) == '*' || currentStr.charAt(i) == '/')
                    break;
                else if(currentStr.charAt(i) == '.')
                {
                    //do nothing
                }
                else
                    count ++;
            }
            if(count >= 9)
                return false;
            else
                return true;
        }
    }

    // convert infix to postfix
    public void shuntingYard()
    {
        char lastChar = currentStr.charAt(currentStr.length() -1);

        // if last char is operand, igonore it
        if(lastChar == '\uFF0B' || lastChar == '\u2014' || lastChar == '*' || lastChar == '/' || lastChar == '.')
        {
            currentStr = currentStr.substring(0, currentStr.length()-1);
        }

        // reorganize the input and put it into infix list
        String tempString = "";
        for(int i = 0; i < currentStr.length(); ++i)
        {
            if(currentStr.charAt(i) == '\uFF0B' || currentStr.charAt(i) == '\u2014' || currentStr.charAt(i) == '*' ||currentStr.charAt(i) == '/')
            {
                infix.add(tempString);
                tempString = "";
                infix.add(String.valueOf(currentStr.charAt(i)));
            }
            else
            {
                tempString += String.valueOf(currentStr.charAt(i));
            }
        }
        infix.add(tempString);

        //infix to postfix, adapted from my old project, similar with minor changes
        for(int i = 0; i < infix.size(); ++i)
        {
            // condition : plus sign or minus sign
            if (infix.get(i).equals("\uFF0B") || infix.get(i).equals("\u2014"))
            {
                Boolean stackIsNotEmpty = !stack.empty();
                while(stackIsNotEmpty)
                {
                    if(stack.peek().equals("\uFF0B") || stack.peek().equals("\u2014")  || stack.peek().equals("*") || stack.peek().equals("/"))
                    {
                        postfix.add(stack.pop());
                        stackIsNotEmpty = !stack.empty();
                    }
                    else
                        stackIsNotEmpty = false;
                }
                stack.push(infix.get(i));
            }

            // condition : multiplication sign or division sign
            else if (infix.get(i).equals("*") || infix.get(i).equals("/"))
            {
                Boolean stackIsNotEmpty = !stack.empty();
                while(stackIsNotEmpty)
                {
                    if(stack.peek().equals("*") || stack.peek().equals("/"))
                    {
                        postfix.add(stack.pop());
                        stackIsNotEmpty = !stack.empty();
                    }
                    else
                        stackIsNotEmpty = false;
                }
                stack.push(infix.get(i));
            }
            // condition : number
            else
            {
                postfix.add(infix.get(i));
            }

        }
        // at the end of process, make sure stack is empty
        while(!stack.empty())
        {
            postfix.add(stack.pop());
        }
    }

    // evaluate postfix, if is operand, pop two number and push the result into stack, if it is a number, push into stack
    public void evaluatePostfix()
    {
        String conditionMessage = "good";
        for(int i = 0; i < postfix.size(); ++i)
        {
            BigDecimal firstNumber, secondNumber;
            if(postfix.get(i).equals("\uFF0B"))
            {
                secondNumber = postfixStack.pop();
                firstNumber = postfixStack.pop();
                postfixStack.push(firstNumber.add(secondNumber));
            }
            else if (postfix.get(i).equals("\u2014"))
            {
                secondNumber = postfixStack.pop();
                firstNumber = postfixStack.pop();
                postfixStack.push(firstNumber.subtract(secondNumber));
            }
            else if (postfix.get(i).equals("*"))
            {
                secondNumber = postfixStack.pop();
                firstNumber = postfixStack.pop();
                postfixStack.push(firstNumber.multiply(secondNumber));
            }
            else if (postfix.get(i).equals("/"))
            {
                BigDecimal result;
                secondNumber = postfixStack.pop();
                firstNumber = postfixStack.pop();
                // you cant divide a 0, error
                if(secondNumber.compareTo(BigDecimal.ZERO) == 0)
                {
                    conditionMessage = "Error";
                    break;
                }
                try
                {
                    result = firstNumber.divide(secondNumber);
                }
                catch(ArithmeticException e)
                {
                    result = firstNumber.divide(secondNumber, 9, BigDecimal.ROUND_HALF_EVEN);
                }
                postfixStack.push(result);
            }
            else
            {
                postfixStack.push(new BigDecimal(postfix.get(i)));
            }
        }

        if(conditionMessage.equals("Error"))
        {
            clear();
            screen.setText("Error");
        }
        else
        {
            BigDecimal result = postfixStack.pop();
            result = result.round(new MathContext(9, RoundingMode.HALF_EVEN));
            currentStr = String.valueOf(result);


            //format number nicely, remove all the trailing zeros
            boolean hasDot = false;
            boolean hasE = false;
            int positionE = 0;
            for (int i = 0; i < currentStr.length(); ++i)
            {
                if(currentStr.charAt(i) == '.')
                    hasDot = true;
                if(currentStr.charAt(i) == 'E')
                {
                    hasE = true;
                    positionE = i;
                }
            }
            // remove trailing 0s if it is a decimal number
            if(hasDot && !hasE)
            {
                char lastChar = currentStr.charAt(currentStr.length() - 1);
                while (lastChar == '0' && currentStr.length() > 1)
                {
                    currentStr = currentStr.substring(0, currentStr.length() - 1);
                    lastChar = currentStr.charAt(currentStr.length() - 1);
                }
                if(lastChar == '.')
                    currentStr = currentStr.substring(0, currentStr.length() - 1);
            }
            // remove trailing zeros if it is a scientific notation
            if(hasE)
            {
                String beginPart = currentStr.substring(0, positionE);
                String endPart = currentStr.substring(positionE, currentStr.length());

                char lastChar = beginPart.charAt(beginPart.length() - 1);
                while (lastChar == '0' && beginPart.length() > 1)
                {
                    beginPart = beginPart.substring(0, beginPart.length() - 1);
                    lastChar = beginPart.charAt(beginPart.length() - 1);
                }
                if(lastChar == '.')
                    beginPart = currentStr.substring(0, beginPart.length() - 1);

                // remove trailing zeros, from https://stackoverflow.com/questions/14984664/remove-trailing-zero-in-java
                //beginPart = beginPart.indexOf(".") < 0 ? beginPart : beginPart.replaceAll("0*$", "").replaceAll("\\.$", "");
                currentStr = beginPart + endPart;
            }
            screen.setText(currentStr);
        }

        infix.clear();
        postfix.clear();
        stack.clear();
        postfixStack.clear();
    }
}
