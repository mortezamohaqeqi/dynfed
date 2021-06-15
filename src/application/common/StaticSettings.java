package application.common;

import java.util.Arrays;
import java.util.List;

import application.common.enums.EPolicy;
import application.common.enums.EPreemptMethod;

public class StaticSettings 
{
	final static public EPolicy[] EXCLUDED_POLICY = {EPolicy.NPFederated, EPolicy.RMNecessaryTest};
	final static public String DEFAULT_THREADS = "Preferred";
}
