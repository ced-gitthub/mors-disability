package com.ced.disabilities.disability;

/** Client-side only: DisabilityClient mutes the master volume while owned. */
public class DeafDisability extends Disability {
	public DeafDisability() {
		super("deaf", "Deaf", "Profound hearing loss: you cannot hear anything");
	}
}