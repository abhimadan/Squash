package com.example.squash;

import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/*This class defines the view that the animations are drawn to.*/
public class BounceView extends View { 
	final long NANO_MULTIPLE = (long) Math.pow(10,9); //Used to convert nanoseconds into seconds
	Paint white,paint;
	Canvas draw; // Main canvas
	Bitmap panel; //bitmap which is drawn to draw
	DisplayMetrics metrics;
	ArrayList<Balloon> balloons; // Stores balloons
	Balloon balloon,tempBalloon;
	int curLevel,collisionAngle;
	long startTime,endTime;
	float paddleX,paddleY; // paddle position
	boolean [] [] previousCollision; // stores whether balloons have collided recently
	boolean started, gameOver;
	
	public BounceView (Context context) { // Initialization
		super (context);
		metrics = context.getResources().getDisplayMetrics();
		panel = Bitmap.createBitmap (metrics.widthPixels,
				metrics.heightPixels,Bitmap.Config.ARGB_8888);
		
		draw = new Canvas (panel);
		balloons = new ArrayList <Balloon> ();
		
		curLevel = 0;
		
		paddleX = metrics.widthPixels*(2/5);
		paddleY = metrics.heightPixels*(9/10);
		
		white = new Paint ();
		white.setARGB (255,255,255,255);

		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.RED);
		paint.setTextSize(35);
		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.MONOSPACE);
		
		started = false;
		gameOver = false;
		
		previousCollision = new boolean [5] [5];
		//fills the array; no collisions detected
		for (int i = 0; i < 5; i++) {
			Arrays.fill (previousCollision[i],false);
		}
	}
	
	/* This method monitors and handles touch events 
	 * e - the detected MotionEvent
	 **/
	@Override	
	public boolean onTouchEvent (MotionEvent e) { 
		int action = e.getAction();
		
		if (action == MotionEvent.ACTION_DOWN && !started && (System.nanoTime()-endTime)/NANO_MULTIPLE >= 2) {
			started = true;
			gameOver = false;
			startTime = System.nanoTime();
		}		
			
		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
			paddleX = e.getX();
			if (paddleX + 100 > metrics.widthPixels) 
				paddleX = metrics.widthPixels - 100;
			else if (paddleX - 100 < 0) 
				paddleX = 100;			
		}
		
		return true;
	}
	
	
	/* This method draws the animation and 
	 * actively controls variables
	 * canvas - the Canvas which is drawn to
	 **/
	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw (Canvas canvas) { 		
		if (started && !gameOver){
			if (curLevel < 5 && System.nanoTime()-startTime >= 8 * curLevel * NANO_MULTIPLE) {
				curLevel++;
				balloons.add (new Balloon (curLevel,30,30));
			}
			
			draw.drawColor (Color.BLACK);
			draw.drawRect (paddleX-100,paddleY,paddleX+100,paddleY+50,white); // Draws paddle
			
			for (int i = 0; i < curLevel; i++) {
				balloon = balloons.get(i);
				
				draw.drawCircle (balloon.getX(),balloon.getY(), // Draws balloon
						30,balloon.getPaint());
				
				balloon.setX((int)Math.round(balloon.getX()+balloon.getTrajectoryX()));
				balloon.setY((int)Math.round(balloon.getY()+balloon.getTrajectoryY()));
				
				for (int j = 0; j < curLevel; j++) {
					tempBalloon = balloons.get(j);
					
					if (j != i && Math.hypot(tempBalloon.getX()-balloon.getX(),
							tempBalloon.getY()-balloon.getY()) <= 60 && !previousCollision[i][j]) {
						
						previousCollision[i][j] = true;
						
						collisionAngle = (int) Math.toDegrees (Math.atan2 (balloon.getY()-tempBalloon.getY(),
								balloon.getX()-tempBalloon.getX()));
						
						balloon.setX ((int)Math.floor (60 * Math.cos(Math.toRadians(collisionAngle)))
								+ tempBalloon.getX());
						balloon.setY ((int)Math.floor (60 * Math.sin(Math.toRadians(collisionAngle)))
								+ tempBalloon.getY());	
						
						collision (balloon,tempBalloon);
						
					} else if (j != i && Math.hypot(tempBalloon.getX()-balloon.getX(),
							tempBalloon.getY()-balloon.getY()) > 60 && previousCollision[i][j])
						
						previousCollision[i][j] = false;
				}
				
				if (balloon.getX()+30 >= metrics.widthPixels) {
					balloon.changeTrajectory(true);
					balloon.setX(metrics.widthPixels-30);
				} else if (balloon.getX()-30 <= 0) {
					balloon.changeTrajectory(true);
					balloon.setX(30);
				} 
				
				if (balloon.getY()+330 >= metrics.heightPixels
						&& Math.abs(balloon.getX()-paddleX)<=100) {
					balloon.changeTrajectory(false);
					balloon.setY(metrics.heightPixels-330);
				} else if (balloon.getY()-30 <= 0) {
					balloon.changeTrajectory(false);
					balloon.setY(30);
				} else if (balloon.getY()+270 >= metrics.heightPixels) {
					//GAME OVER!!!
					//resets values to prepare them for a new game
					started = false;
					gameOver = true;
					balloons.clear();  
					for (int j = 0; j < 5; j++) {
						Arrays.fill (previousCollision[j],false);
					}
					
					curLevel = 0;
					endTime = System.nanoTime();
				}
				
			}
			
			canvas.drawBitmap(panel, 0, 0, null); 
			
			invalidate(); // Updates view
		} else {

			draw.drawText("Touch to Start",metrics.widthPixels/2 - 150,
					metrics.heightPixels/2-150,paint);
			if (gameOver == true ){
				draw.drawText("Score: " + (endTime - startTime)/NANO_MULTIPLE* 100,metrics.widthPixels/2 - 180,
					metrics.heightPixels/2-250,paint);
			}
			
			canvas.drawBitmap (panel,0,0,null);
			invalidate();
		}
	}
	
	/* This method detects a collision between balloons
	 * balloon1 - the balloon whose trajectory is changed
	 * balloon2 - the balloon which collides with balloon1
	 **/
	private void collision (Balloon balloon1,Balloon balloon2) { 
		double incidenceAngle, mirrorLineAngle;
		
		mirrorLineAngle = Math.toDegrees(Math.atan2 (balloon1.getX()-balloon2.getX(),
				balloon2.getY()-balloon1.getY()));
		incidenceAngle = Math.toDegrees(Math.atan2 (balloon1.getTrajectoryY(),
				balloon1.getTrajectoryX()));
		
		balloon1.setAngle((int)Math.round(180 + 2*mirrorLineAngle - incidenceAngle));
		
		if (balloons.indexOf(balloon2) < balloons.indexOf(balloon1))	//recognizes and changes trajectory of first balloon
			collision (balloon2,balloon1);								//to ensure the collision applies to both balloons
		
	}
	
}

