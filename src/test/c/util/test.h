
#ifndef TEST_H
#define TEST_H

int assertEquals(int a, int b, char * msg);
int assertNotEquals(int a, int b, char * msg);

int assertEqualsPtr(void * a, void * b, char * msg);
int assertNotEqualsPtr(void * a, void * b, char * msg);

int assertEqualsFloat(float a, float b, char * msg);
int assertNotEqualsFloat(float a, float b, char * msg);

#endif