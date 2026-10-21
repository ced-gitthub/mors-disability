package com.ced.disabilities.disability;

/** Client-side only: DisabilityClient washes the screen out to grey while owned. */
public class ColorBlindDisability extends Disability {
	public ColorBlindDisability() {
		super("color_blind", "Color Blind", "Colour vision deficiency: the world loses its colour");
	}
}
