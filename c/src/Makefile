CC = gcc
CFLAGS = -shared -I /usr/opt/java122/include -I /usr/opt/java122/include/alpha -threads

HAPLOS = libhaplos.so
HAPLOS_OBJS = haplolib.o rP4_procedure.o new_block_em.o
HAPLOS_LIBS = -lm

all: $(HAPLOS)

$(HAPLOS): $(HAPLOS_OBJS)
	$(CC) $(CFLAGS) -o $@ $(HAPLOS_OBJS) $(HAPLOS_LIBS)

clean:
	rm -f $(HAPLOS) $(HAPLOS_OBJS) *~ core
