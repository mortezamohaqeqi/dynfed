package application.common.enums;

public enum EPreemptMethod {
	Non_Preemptive,	       	/* NonPreemptive */ 
	Preemptive,          	/* Preemptive */
	Ticked_Preemptive,      /* Preemptive only on ticks, but schedule on each event (job finish) as well  */
	NW_Ticked_Preemptive, 		/* Preempt and Schedule only on Ticks */
	Ticked_Adaptive, 		/* Non-preemptive, Schedule only on Ticks. Ticks are not equidistance, and are unknown. */
}