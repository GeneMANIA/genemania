

# some data used in unit tests and reference calculations

# MultiOPCSymMatrixTest

c = [
       0.00000   0.88715   0.67974   0.69090;
       0.88715   0.00000   0.99739   0.70729;
       0.67974   0.99739   0.00000   0.97997;
       0.69090   0.70729   0.97997   0.00000;
    ]
    
a = [
        0   1   1   0   0   1;
        0   1   1   1   0   0;
        0   1   1   0   1   1;
        1   0   1   1   1   0;
    ]
    
w = [
        0.18519;
        0.70168;
        0.57522;
        0.56964;
        0.59336;
        0.99504;
    ]
    
x = ones(4, 1);

#format long

result = (c + a*diag(w)*a')*x


result = c + a*diag(w)*a'

result(3,4)