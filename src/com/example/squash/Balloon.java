package com.example.squash;

import android.graphics.Paint;
import android.graphics.Paint.Style;

/* This class stores information about the balloons
 * that bounce around the screen.
 **/
public class Balloon {
	private int x,y,speed,angle;
	private double trajectoryX, trajectoryY;
	private Paint paint;  //paint which is used to colour the balloon
	
	
	public Balloon (int level, int startX, int startY) {
		x = startX; //sets position of centre's x-coordinate
		y = startY; //sets position of centre's y-coordinate
		speed = level*12;
		angle = (int)Math.floor(Math.random()*89) + 1;
		trajectoryX = speed * Math.cos (Math.toRadians(angle));
		trajectoryY = speed * Math.sin (Math.toRadians(angle));
		paint = new Paint ();
		
		paint.setStyle (Style.FILL);
		
		//paint configurations for 5 levels
		switch (level) {
		case 1: paint.setARGB (255,255,0,0);
				break;
		case 2: paint.setARGB (255,0,255,0);
				break;
		case 3: paint.setARGB (255,0,0,255);
				break;
		case 4: paint.setARGB (255,255,0,255);
				break;
		case 5: paint.setARGB (255,0,255,255);
				break;
		}
	}
	
	/* This method changes the x-trajectory (if component == true)
	 * or the y-trajectory (if component == false).
	 **/
	public void changeTrajectory (boolean component) { // Method for changing balloon trajectory after collision
		if (component) {
			trajectoryX *= -1;
		} 
		if (!component) {
			trajectoryY *= -1;
		}
	}

	public int getY() { //Gets the y position of the balloon
		return y;
	}

	public int getX() { //Gets the x position of the balloon
		return x;
	}
	
	public double getTrajectoryX() { // gets the horizontal trajectory of the balloon
		return trajectoryX;
	}
	
	public double getTrajectoryY() { // gets the vertical trajectory of the balloon
		return trajectoryY;
	}
	
	public Paint getPaint() { // Gets balloon paint
		return paint;
	}
	
	public void setY(int y) { // Sets balloon y position
		this.y = y;
	}

	public void setX(int x) { // Sets balloon x position
		this.x = x;
	}
	
	public void setAngle(int angle) { // Sets angle of balloon trajectory
		this.angle = angle;
		trajectoryX = speed * Math.cos (Math.toRadians(angle));
		trajectoryY = speed * Math.sin (Math.toRadians(angle));
	}

	
}
