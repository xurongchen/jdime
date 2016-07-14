package de.fosd.jdime.matcher.cost_model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.fosd.jdime.common.Artifact;
import de.fosd.jdime.common.ArtifactList;
import de.fosd.jdime.common.Artifacts;
import de.fosd.jdime.common.MergeContext;
import de.fosd.jdime.matcher.MatcherInterface;
import de.fosd.jdime.matcher.matching.Matching;
import de.fosd.jdime.matcher.matching.Matchings;

public class CostModelMatcher<T extends Artifact<T>> implements MatcherInterface<T> {

    private static final Logger LOG = Logger.getLogger(CostModelMatcher.class.getCanonicalName());

    @FunctionalInterface
    private interface SimpleWeightFunction<T extends Artifact<T>> {

        float weigh(CostModelMatching<T> matching);
    }

    @FunctionalInterface
    private interface WeightFunction<T extends Artifact<T>> {

        float weigh(CostModelMatching<T>  matching, float quantity);
    }

    /**
     * The cost of not matching an artifact.
     */
    private float wn;

    /**
     * The function determining the cost of renaming an artifact. This cost is 0 if the artifacts match according to
     * {@link Artifact#matches}.
     */
    private SimpleWeightFunction<T> wr;

    /**
     * The function determining the cost of ancestry violations.
     */
    private WeightFunction<T> wa;

    /**
     * The function determining the cost of breaking up sibling groups.
     */
    private WeightFunction<T> ws;

    private Random rng;

    public void setNoMatchWeight(float wn) {
        this.wn = wn;
    }

    public void setRenamingWeight(float wr) {
        setRenamingWeight(matching -> wr);
    }

    public void setRenamingWeight(SimpleWeightFunction<T> wr) {
        this.wr = wr;
    }

    public void setAncestryViolationWeight(float wa) {
        setAncestryViolationWeight((matching, quantity) -> wa * quantity);
    }

    public void setAncestryViolationWeight(WeightFunction<T> wa) {
        this.wa = wa;
    }

    public void setSiblingGroupBreakupWeight(float ws) {
        setSiblingGroupBreakupWeight((matching, quantity) -> ws * quantity);
    }

    public void setSiblingGroupBreakupWeight(WeightFunction<T> ws) {
        this.ws = ws;
    }

    public float cost(Matchings<T> matchings) {
        return cost(matchings.stream().map(m -> new CostModelMatching<>(m.getLeft(), m.getRight())).collect(Collectors.toList()));
    }

    /**
     * Returns the exact cost of the given <code>matchings</code>. This assumes that <code>matchings</code> contains
     * for every node in the left and right tree exactly one <code>CostModelMatching</code> containing the node.
     *
     * @param matchings
     *         the <code>CostModelMatching</code>s to evaluate
     * @return the cost based on the set weight functions
     */
    private float cost(List<CostModelMatching<T>> matchings) {

        if (matchings.isEmpty()) {
            return 0;
        }

        CostModelMatching<T> first = matchings.get(0);
        int t1Size = first.m.findRoot().getTreeSize();
        int t2Size = first.n.findRoot().getTreeSize();

        return (1.0f / (t1Size + t2Size)) * matchings.stream().collect(Collectors.summingDouble(m -> exactCost(m, matchings))).floatValue();
    }

    private float exactCost(CostModelMatching<T> matching, List<CostModelMatching<T>> matchings) {

        if (matching.isNoMatch()) {
            return wn;
        }

        return renamingCost(matching) + ancestryViolationCost(matching, matchings) + siblingGroupBreakupCost(matching, matchings);
    }

    private float ancestryViolationCost(CostModelMatching<T> matching, List<CostModelMatching<T>> matchings) {
        return wa.weigh(matching, numAncestryViolatingChildren(matching, matchings) + numAncestryViolatingChildren(matching, matchings));
    }

    private int numAncestryViolatingChildren(CostModelMatching<T> matching, List<CostModelMatching<T>> matchings) {
        ArtifactList<T> mChildren = matching.m.getChildren();
        ArtifactList<T> nChildren = matching.n.getChildren();

        return (int) mChildren.stream().map(mAp -> image(mAp, matchings)).filter(nChildren::contains).count();
    }

    private float siblingGroupBreakupCost(CostModelMatching<T> matching, List<CostModelMatching<T>> matchings) {
        List<T> dMm, iMm, fMm;
        List<T> dMn, iMn, fMn;

        dMm = siblingDivergentSubset(matching.m, matching.n, matchings);
        iMm = siblingInvariantSubset(matching.m, matching.n, matchings);
        fMm = distinctSiblingFamilies(matching.m, matchings);

        dMn = siblingDivergentSubset(matching.n, matching.m, matchings);
        iMn = siblingInvariantSubset(matching.n, matching.m, matchings);
        fMn = distinctSiblingFamilies(matching.n, matchings);

        float mCost = (float) dMm.size() / (iMm.size() + fMm.size());
        float nCost = (float) dMn.size() / (iMn.size() + fMn.size());
        return ws.weigh(matching, mCost + nCost);
    }

    private List<T> siblingInvariantSubset(T m, T n, List<CostModelMatching<T>> matchings) {
        return siblings(m).stream().filter(mAp -> siblings(n).contains(image(mAp, matchings))).collect(Collectors.toList());
    }

    private List<T> siblingDivergentSubset(T m, T n, List<CostModelMatching<T>> matchings) {
        List<T> inv = siblingInvariantSubset(m, n, matchings);
        return siblings(m).stream().filter(mAp -> !inv.contains(mAp) && image(mAp, matchings) != null).collect(Collectors.toList());
    }

    private List<T> distinctSiblingFamilies(T m, List<CostModelMatching<T>> matchings) {
        return siblings(m).stream().map(mAp -> image(mAp, matchings).getParent()).collect(Collectors.toList());
    }

    private T image(T m, List<CostModelMatching<T>> matchings) {
        //TODO this is very inefficient...
        return matchings.stream().filter(ma -> ma.contains(m)).map(ma -> ma.other(m)).findFirst().get();
    }

    /**
     * Sets the bounds ({@link CostModelMatching#setBounds(Bounds)}) for the cost of the given <code>matching</code>
     * based on the given <code>currentMatchings</code>.
     *
     * @param matching
     *         the <code>CostModelMatching</code> whose costs are to be bounded
     * @param currentMatchings
     *         the current <code>CostModelMatching</code>s being considered
     */
    private void boundCost(CostModelMatching<T> matching, List<CostModelMatching<T>> currentMatchings) {

        if (matching.isNoMatch()) {
            matching.setBounds(wn, wn);
            return;
        }

        float cR = renamingCost(matching);
        Bounds cABounds = boundAncestryViolationCost(matching, currentMatchings);
        Bounds cSBounds = boundSiblingGroupBreakupCost(matching, currentMatchings);

        matching.setBounds(cR + cABounds.getLower() + cSBounds.getLower(), cR + cABounds.getUpper() + cSBounds.getUpper());
    }

    private float renamingCost(CostModelMatching<T> matching) {
        if (matching.m.matches(matching.n)) {
            return 0;
        } else {
            return wr.weigh(matching);
        }
    }

    private Bounds boundAncestryViolationCost(CostModelMatching<T> matching, List<CostModelMatching<T>> currentMatchings) {
        T m = matching.m;
        T n = matching.n;

        Stream<T> mLower = m.getChildren().stream().filter(mAp -> ancestryIndicator(mAp, n, currentMatchings, false));
        Stream<T> nLower = n.getChildren().stream().filter(nAp -> ancestryIndicator(nAp, m, currentMatchings, false));

        Stream<T> mUpper = m.getChildren().stream().filter(mAp -> ancestryIndicator(mAp, n, currentMatchings, true));
        Stream<T> nUpper = n.getChildren().stream().filter(nAp -> ancestryIndicator(nAp, m, currentMatchings, true));

        int lowerBound = (int) (mLower.count() + nLower.count());
        int upperBound = (int) (mUpper.count() + nUpper.count());

        return new Bounds(wa.weigh(matching, lowerBound), wa.weigh(matching, upperBound));
    }

    private boolean ancestryIndicator(T mAp, T n, List<CostModelMatching<T>> g, boolean upper) {

        if (upper) {
            return g.stream().anyMatch(match -> match.m == mAp && !(match.n == null || n.getChildren().contains(match.n)));
        } else {
            return g.stream().noneMatch(match -> match.m == mAp && (match.n == null || n.getChildren().contains(match.n)));
        }
    }

    private Bounds boundSiblingGroupBreakupCost(CostModelMatching<T> matching, List<CostModelMatching<T>> currentMatchings) {
        T m = matching.m;
        T n = matching.n;

        Bounds dMN = boundDistinctSiblingGroups(m, n, currentMatchings);
        Bounds dNM = boundDistinctSiblingGroups(n, m, currentMatchings);

        Bounds iMN = boundInvariantSiblings(m, n, currentMatchings);
        Bounds iNM = boundInvariantSiblings(n, m, currentMatchings);

        float lower = ws.weigh(matching, ((dMN.getLower() / (iMN.getUpper() * (dMN.getLower() + 1))) + (dNM.getLower() / (iNM.getUpper() * (dNM.getLower() + 1)))));
        float upper = ws.weigh(matching, (dMN.getUpper() / iMN.getLower()) + (dNM.getUpper() / iNM.getLower())) / 2;

        return new Bounds(lower, upper);
    }

    private Bounds boundDistinctSiblingGroups(T m, T n, List<CostModelMatching<T>> currentMatchings) {
        long lower = m.getChildren().stream().filter(mAp -> distinctSiblingIndicator(mAp, n, currentMatchings, false)).count();
        long upper = m.getChildren().stream().filter(mAp -> distinctSiblingIndicator(mAp, n, currentMatchings, true)).count();

        return new Bounds(lower, upper);
    }

    private boolean distinctSiblingIndicator(T mAp, T n, List<CostModelMatching<T>> g, boolean upper) {

        if (upper) {
            return g.stream().anyMatch(match -> match.m == mAp && !(match.n == null || otherSiblings(n).contains(match.n)));
        } else {
            return g.stream().noneMatch(match -> match.m == mAp && (match.n == null || otherSiblings(n).contains(match.n)));
        }
    }

    private Bounds boundInvariantSiblings(T m, T n, List<CostModelMatching<T>> currentMatchings) {
        long lower = m.getChildren().stream().filter(mAp -> invariantSiblingIndicator(mAp, n, currentMatchings, false)).count();
        long upper = m.getChildren().stream().filter(mAp -> invariantSiblingIndicator(mAp, n, currentMatchings, true)).count();

        return new Bounds(lower + 1, upper + 1);
    }

    private boolean invariantSiblingIndicator(T mAp, T n, List<CostModelMatching<T>> g, boolean upper) {

        if (upper) {
            return g.stream().anyMatch(match -> match.m == mAp && otherSiblings(n).contains(match.n));
        } else {
            return g.stream().allMatch(match -> (match.m != mAp) || otherSiblings(n).contains(match.n));
        }
    }

    private List<T> siblings(T artifact) {
        T parent = artifact.getParent();

        if (parent == null) {
            return Collections.emptyList();
        } else {
            List<T> res = new ArrayList<T>(parent.getChildren());
            res.remove(artifact);

            return res;
        }
    }

    private List<T> otherSiblings(T artifact) {
        List<T> siblings = siblings(artifact);
        siblings.remove(artifact);

        return siblings;
    }

    @Override
    public Matchings<T> match(MergeContext context, T left, T right) {
        setNoMatchWeight(context.wn);
        setRenamingWeight(context.wr);
        setAncestryViolationWeight(context.wa);
        setSiblingGroupBreakupWeight(context.ws);
        rng = context.seed.map(Random::new).orElse(new Random());

        float beta = 1; // TODO figure out good values for this (dependant on the size of the trees)

        List<CostModelMatching<T>> m = initialize(context.pAssign, left, right);
        List<CostModelMatching<T>> mHat;

        for (int i = 0; i < context.costModelIterations; i++) {
            mHat = propose(context.pAssign, left, right, m);

            if (chance(acceptanceProb(beta, m, mHat))) {
                m = mHat;
            }
        }

        //TODO use the last m or find the m with the lowest cost? Probably the latter.

        return convert(m);
    }

    private Matchings<T> convert(List<CostModelMatching<T>> matchings) {
        return matchings.stream()
                        .filter(((Predicate<CostModelMatching<T>>) CostModelMatching::isNoMatch).negate())
                        .map(m -> new Matching<>(m.m, m.n, 0))
                        .collect(Matchings::new, Matchings::add, Matchings::addAll);
    }

    private List<CostModelMatching<T>> propose(float pAssign, T left, T right, List<CostModelMatching<T>> m) {
        int j = rng.nextInt(m.size());
        List<CostModelMatching<T>> fixed = new ArrayList<>(m.subList(0, j));

        return complete(fixed, pAssign, left, right);
    }

    private List<CostModelMatching<T>> initialize(float pAssign, T left, T right) {
        return complete(new ArrayList<>(), pAssign, left, right);
    }

    private List<CostModelMatching<T>> complete(List<CostModelMatching<T>> matchings, float pAssign, T left, T right) {
        List<CostModelMatching<T>> g = completeBipartiteGraph(left, right);

        matchings.forEach(m -> prune(m, g));

        while (!g.isEmpty()) {
            g.forEach(m -> boundCost(m, g));
            Collections.sort(g, Comparator.comparing(CostModelMatching::getBounds));

            CostModelMatching<T> matching = null;

            for(ListIterator<CostModelMatching<T>> it = g.listIterator(); it.hasNext();) {
                matching = it.next();

                if (chance(pAssign)) {
                    it.remove();
                    break;
                }
            }

            if (matching == null) {
                continue;
            }

            /** TODO
             * "If the candidate edge can be fixed (?) and at least one complete matching still exists (???)"
             *
             * if (???) {
             *     continue;
             * }
             */

            matchings.add(matching);
            prune(matching, g);
        }

        return matchings;
    }

    private void prune(CostModelMatching<T> matching, List<CostModelMatching<T>> g) {

        for (ListIterator<CostModelMatching<T>> it = g.listIterator(); it.hasNext();) {
            CostModelMatching<T> current = it.next();

            if ((matching.m != null && current.m == matching.m) || (matching.n != null && current.n == matching.n)) {
                it.remove();
            }
        }
    }

    private boolean chance(float p) {
        return rng.nextFloat() < p;
    }

    private List<CostModelMatching<T>> completeBipartiteGraph(T left, T right) {
        List<T> leftNodes = Artifacts.bfs(left);
        List<T> rightNodes = Artifacts.bfs(right);

        // add the "No Match" node
        leftNodes.add(null);
        rightNodes.add(null);

        List<CostModelMatching<T>> bipartiteGraph = new LinkedList<>();

        for (T lNode : leftNodes) {
            for (T rNode : rightNodes) {

                if (lNode == null && rNode == null) {
                    continue;
                }

                bipartiteGraph.add(new CostModelMatching<T>(lNode, rNode));
            }
        }

        return bipartiteGraph;
    }

    private float objective(float beta, List<CostModelMatching<T>> matchings) {
        return (float) Math.exp(-(beta * cost(matchings)));
    }

    private float acceptanceProb(float beta, List<CostModelMatching<T>> m, List<CostModelMatching<T>> mHat) {
        return Math.min(1, objective(beta, mHat) / objective(beta, m));
    }
}
