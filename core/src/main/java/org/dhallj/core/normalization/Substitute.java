package org.dhallj.core.normalization;

import java.util.LinkedList;
import java.util.List;
import org.dhallj.core.Expr;
import org.dhallj.core.LetBinding;
import org.dhallj.core.visitor.IdentityVis;

/**
 * Substitutes an expression for all instances of a variable in another expression.
 *
 * <p>Note that this visitor maintains internal state and instances should not be reused.
 */
public final class Substitute extends IdentityVis {
  private final String name;
  private int index = 0;
  private final LinkedList<Expr> replacementStack = new LinkedList<>();

  public Substitute(String name, Expr replacement) {
    this.name = name;
    this.replacementStack.push(replacement);
  }

  @Override
  public void bind(String name, Expr type) {
    this.replacementStack.push(this.replacementStack.get(0).increment(name));

    if (name.equals(this.name)) {
      this.index += 1;
    }
  }

  @Override
  public Expr onIdentifier(Expr self, String name, long index) {
    if (name.equals(this.name)) {
      if (index == this.index) {
        return this.replacementStack.get(0);
      } else if (index > this.index) {
        return Expr.makeIdentifier(name, index - 1);
      } else {
        return self;
      }
    } else {
      return self;
    }
  }

  @Override
  public Expr onLambda(String name, Expr type, Expr result) {
    this.replacementStack.pop();

    if (name.equals(this.name)) {
      this.index -= 1;
    }

    return Expr.makeLambda(name, type, result);
  }

  @Override
  public Expr onPi(String name, Expr type, Expr result) {
    this.replacementStack.pop();

    if (name.equals(this.name)) {
      this.index -= 1;
    }

    return Expr.makePi(name, type, result);
  }

  @Override
  public Expr onLet(List<LetBinding<Expr>> bindings, Expr body) {
    for (LetBinding<Expr> binding : bindings) {
      String name = binding.getName();

      this.replacementStack.pop();

      if (name.equals(this.name)) {
        this.index -= 1;
      }
    }
    return Expr.makeLet(bindings, body);
  }
}
