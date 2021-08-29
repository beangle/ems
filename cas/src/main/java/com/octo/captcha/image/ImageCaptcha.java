/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.octo.captcha.image;

import com.octo.captcha.Captcha;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The inside ImageCaptcha class dependens com.sun.image.codec.jpeg.* which has been droped.
 * So rewite it.
 **/
public abstract class ImageCaptcha implements Captcha {
  private Boolean hasChallengeBeenCalled;
  protected String question;
  protected transient BufferedImage challenge;

  protected ImageCaptcha(String question, BufferedImage challenge) {
    this.hasChallengeBeenCalled = Boolean.FALSE;
    this.challenge = challenge;
    this.question = question;
  }

  public final String getQuestion() {
    return this.question;
  }

  public final Object getChallenge() {
    return this.getImageChallenge();
  }

  public final BufferedImage getImageChallenge() {
    this.hasChallengeBeenCalled = Boolean.TRUE;
    return this.challenge;
  }

  public final void disposeChallenge() {
    this.challenge = null;
  }

  public Boolean hasGetChalengeBeenCalled() {
    return this.hasChallengeBeenCalled;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    if (this.challenge != null) {
      ImageIO.write(challenge, "jpg", out);
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.challenge = ImageIO.read(in);
  }
}
