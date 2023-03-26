package com.example.brickbreaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class GameView extends View {

    Context context;
    float ballX;
    float ballY;
    Velocity velocity = new Velocity(25, 32);
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    Paint brickPaint = new Paint();
    float TEXT_SIZE = 120;
    float paddleX;
    float paddleY;
    float oldX;
    float oldPaddleX;
    int points = 0;
    int life = 3;
    Bitmap ball;
    Bitmap paddle;
    int screenWidth;
    int screenHeight;
    int ballWidth;
    int ballHeight;
    Random random;
    Brick[] bricks = new Brick[30];
    int numBricks = 0;
    int brokenBricks = 0;
    boolean gameOver = false;

    public GameView(Context context) {
        super(context);
        this.context = context;
        ball = BitmapFactory.decodeResource(getResources(), R.drawable.football);
        paddle = BitmapFactory.decodeResource(getResources(), R.drawable.paddle);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        healthPaint.setColor(Color.GREEN);
        brickPaint.setColor(Color.rgb(255, 255, 255));
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        random = new Random();
        ballX = random.nextInt(screenWidth - 50);
        ballY = screenHeight / 3;
        paddleY = (screenHeight * 4) / 5;
        paddleX = screenWidth / 2 - paddle.getWidth() / 2;
        ballWidth = ball.getWidth();
        ballHeight = ball.getHeight();
        createBricks();
    }

    public void createBricks() {
        int brickWidth = screenWidth / 8;
        int brickHeight = screenHeight / 16;
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 3; row++) {
                bricks[numBricks] = new Brick(row, col, brickWidth, brickHeight);
                numBricks++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        ballX += velocity.getX();
        ballY += velocity.getY();
        if ((ballX > screenWidth - ball.getWidth()) || ballX <= 0) {
            velocity.setX(velocity.getX() * -1);
        }
        if (ballY <= 0) {
            velocity.setY(velocity.getY() * -1);
        }
        if (ballY > paddleY + paddle.getHeight()) {
            ballX = 1 + random.nextInt(screenWidth - ball.getWidth());
            ballY = screenHeight / 3;
            velocity.setX(xVelocity());
            velocity.setY(32);
            life--;
            if (life == 0) {
                gameOver = true;
                launchGameOver();
            }
        }
        if (((ballX + ball.getWidth()) >= paddleX) &&
                (ballX <= paddleX + paddle.getWidth()) &&
                (ballY + ball.getHeight() >= paddleY) &&
                (ballY + ball.getHeight() <= paddleY + paddle.getHeight())) {
            velocity.setX(velocity.getX() + 5);
            velocity.setY((velocity.getY() + 5) * -1);
        }
        canvas.drawBitmap(ball, ballX, ballY, null);
        canvas.drawBitmap(paddle, paddleX, paddleY, null);
        for (int i = 0; i < numBricks; i++) {
            if (bricks[i].isVisible()) {
                canvas.drawRoundRect(bricks[i].col * bricks[i].width + 1,
                        bricks[i].row * bricks[i].height + 1,
                        bricks[i].col * bricks[i].width + bricks[i].width - 1,
                        bricks[i].row * bricks[i].height + bricks[i].height - 1,
                        5,
                        5,
                        brickPaint);
            }
        }
        canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);
        if (life == 2) {
            healthPaint.setColor(Color.rgb(255, 165, 0));
        } else if (life == 1) {
            healthPaint.setColor(Color.RED);
        }
        canvas.drawRect(screenWidth - 200,
                30,
                screenWidth - 200 + 60 * life,
                80,
                healthPaint);
        for (int i = 0; i < numBricks; i++) {
            if (bricks[i].isVisible()) {
                if (ballX + ballWidth >= bricks[i].col * bricks[i].width &&
                        ballX <= bricks[i].col * bricks[i].width + bricks[i].width &&
                        ballY <= bricks[i].row * bricks[i].height + bricks[i].height &&
                        ballY <= bricks[i].row * bricks[i].height) {
                    velocity.setY((velocity.getY() + 1) * -1);
                    bricks[i].setInvisible();
                    points += 10;
                    brokenBricks++;
                    if (brokenBricks == 24) {
                        launchGameOver();
                    }
                }
            }
        }
        if (brokenBricks == numBricks) {
            gameOver = true;
        }
        if (!gameOver) {
            handler.postDelayed(runnable, UPDATE_MILLIS);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        if (touchY >= paddleY) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                oldX = event.getX();
                oldPaddleX = paddleX;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                float shift = oldX - touchX;
                float newPaddleX = oldPaddleX - shift;
                if (newPaddleX <= 0) {
                    paddleX = 0;
                } else if (newPaddleX > screenWidth - paddle.getWidth()) {
                    paddleX = screenWidth - paddle.getWidth();
                } else {
                    paddleX = newPaddleX;
                }
            }
        }
        return true;
    }

    private void launchGameOver() {
        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(context, GameOver.class);
        intent.putExtra("points", points);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    private int xVelocity() {
        int[] values = {-35, -30, -25, 25, 30, 35};
        int index = random.nextInt(6);
        return values[index];
    }
}
