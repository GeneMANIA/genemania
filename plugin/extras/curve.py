#!/usr/bin/env python

import sys

def receiver_operating_characteristic(tp, tn, fp, fn):
    fpr = fp / float(fp + tn)
    tpr = tp / float(tp + fn)
    return fpr, tpr

def precision_recall(tp, tn, fp, fn):
    precision = tp / float(tp + fp)
    recall = tp / float(tp + fn)
    return recall, precision
    
types = {
    'roc': receiver_operating_characteristic,
    'pr': precision_recall,
}

def dump_points(statistic, filename):
    classes = []
    scores = []
    thresholds = set()
    
    for line in open(filename, 'rU'):
        values = line.strip().split('\t')
        classes.append(int(values[1]))
        score = float(values[2])
        scores.append(score)
        thresholds.add(score)
    
    thresholds = list(thresholds)
    thresholds.sort()
    
    for threshold in thresholds[1:-1]:
        tn = 0
        tp = 0
        fn = 0
        fp = 0
        for i in range(len(classes)):
            if scores[i] < threshold:
                if classes[i]:
                    fp += 1
                else:
                    tn += 1
            else:
                if classes[i]:
                    tp += 1
                else:
                    fn += 1
                    
        x, y = statistic(tp, tn, fp, fn)
        print "%f\t%f" % (x, y)

if __name__ == '__main__':
    type = sys.argv[1]
    filename = sys.argv[2]
    dump_points(types[type], filename)